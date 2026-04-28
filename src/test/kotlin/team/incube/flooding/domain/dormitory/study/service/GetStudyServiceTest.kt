package team.incube.flooding.domain.dormitory.study.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.impl.GetStudyServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import java.time.LocalDateTime

class GetStudyServiceTest :
    BehaviorSpec({
        val studyRedisAdapter = mockk<StudyRedisAdapter>()
        val userRepository = mockk<UserRepository>()
        val studyBanJpaRepository = mockk<StudyBanJpaRepository>()

        val service = GetStudyServiceImpl(studyRedisAdapter, userRepository, studyBanJpaRepository)

        fun user(
            id: Long,
            studentNumber: Int,
        ) = UserJpaEntity(
            id = id,
            name = "학생$id",
            sex = Sex.MAN,
            email = "student$id@test.com",
            studentNumber = studentNumber,
            role = Role.GENERAL_STUDENT,
            dormitoryRoom = 101,
        )

        given("신청자가 없을 때") {
            `when`("자습 신청자 목록을 조회하면") {
                then("빈 리스트를 반환한다") {
                    every { studyRedisAdapter.getApplicantIds() } returns emptySet()

                    val result = service.execute()

                    result.shouldBeEmpty()
                }
            }
        }

        given("신청자 2명이 있을 때") {
            val user1 = user(id = 1L, studentNumber = 1101)
            val user2 = user(id = 2L, studentNumber = 1102)

            `when`("아무도 자습체크를 하지 않았으면") {
                then("모든 isChecked가 false다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute()

                    result shouldHaveSize 2
                    result.all { !it.isChecked } shouldBe true
                }
            }

            `when`("한 명만 자습체크를 했으면") {
                then("체크한 학생의 isChecked는 true, 나머지는 false다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns setOf(1L)
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute().sortedBy { it.userId }

                    result[0].isChecked shouldBe true
                    result[1].isChecked shouldBe false
                }
            }

            `when`("모두 자습체크를 했으면") {
                then("모든 isChecked가 true다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns setOf(1L, 2L)
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute()

                    result shouldHaveSize 2
                    result.all { it.isChecked } shouldBe true
                }
            }
        }

        given("신청자 중 금지된 학생이 있을 때") {
            val user1 = user(id = 1L, studentNumber = 1101)
            val user2 = user(id = 2L, studentNumber = 1102)
            val banEntity =
                StudyBanJpaEntity(
                    user = user2,
                    bannedAt = LocalDateTime.now().minusDays(1),
                    bannedUntil = LocalDateTime.now().plusDays(6),
                )

            `when`("자습 신청자 목록을 조회하면") {
                then("금지된 학생의 isBanned는 true다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        listOf(banEntity)
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute().sortedBy { it.userId }

                    result[0].isBanned shouldBe false
                    result[1].isBanned shouldBe true
                }
            }
        }

        given("금지되고 체크도 된 학생이 있을 때") {
            val user1 = user(id = 1L, studentNumber = 1101)
            val banEntity =
                StudyBanJpaEntity(
                    user = user1,
                    bannedAt = LocalDateTime.now().minusDays(1),
                    bannedUntil = LocalDateTime.now().plusDays(6),
                )

            `when`("자습 신청자 목록을 조회하면") {
                then("isBanned와 isChecked가 모두 true다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        listOf(banEntity)
                    every { studyRedisAdapter.getAttendanceIds() } returns setOf(1L)
                    every { userRepository.findAllById(any()) } returns listOf(user1)

                    val result = service.execute()

                    result[0].isBanned shouldBe true
                    result[0].isChecked shouldBe true
                }
            }
        }

        given("신청자 3명이 studentNumber 순서가 섞여 있을 때") {
            val user1 = user(id = 1L, studentNumber = 1103)
            val user2 = user(id = 2L, studentNumber = 1101)
            val user3 = user(id = 3L, studentNumber = 1102)

            `when`("자습 신청자 목록을 조회하면") {
                then("studentNumber 오름차순으로 정렬된다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L, 3L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2, user3)

                    val result = service.execute()

                    result[0].studentNumber shouldBe 1101
                    result[1].studentNumber shouldBe 1102
                    result[2].studentNumber shouldBe 1103
                }
            }
        }
    })
