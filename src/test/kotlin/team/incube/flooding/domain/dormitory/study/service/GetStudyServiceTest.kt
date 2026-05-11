package team.incube.flooding.domain.dormitory.study.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.impl.GetStudyServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class GetStudyServiceTest :
    BehaviorSpec({
        val studyRedisAdapter = mockk<StudyRedisAdapter>()
        val userRepository = mockk<UserRepository>()
        val studyBanJpaRepository = mockk<StudyBanJpaRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val clock = Clock.fixed(Instant.parse("2026-04-29T12:00:00Z"), ZoneId.of("Asia/Seoul"))
        val studyProperties =
            StudyProperties(
                openTime = LocalTime.of(20, 0),
                closeTime = LocalTime.of(23, 59),
                maxCount = 50,
                lockKey = "study:lock",
            )

        fun user(
            id: Long,
            studentNumber: Int,
            sex: Sex = Sex.MAN,
        ) = UserJpaEntity(
            id = id,
            name = "학생$id",
            sex = sex,
            email = "student$id@test.com",
            studentNumber = studentNumber,
            role = Role.GENERAL_STUDENT,
            dormitoryRoom = 101,
        )
        val currentUser = user(id = 999L, studentNumber = 9999)

        every { currentUserProvider.getCurrentUser() } returns currentUser
        every { studyRedisAdapter.getApplicationStatus(currentUser.id) } returns null
        every { studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(currentUser.id, any()) } returns false

        val service =
            GetStudyServiceImpl(
                studyRedisAdapter = studyRedisAdapter,
                userRepository = userRepository,
                studyBanJpaRepository = studyBanJpaRepository,
                currentUserProvider = currentUserProvider,
                clock = clock,
                studyProperties = studyProperties,
            )

        given("신청자가 없을 때") {
            `when`("자습 신청자 목록을 조회하면") {
                then("빈 리스트를 반환한다") {
                    every { studyRedisAdapter.getApplicantIds() } returns emptySet()
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()

                    val result = service.execute()

                    result.applicants.shouldBeEmpty()
                    result.myApplicationStatus shouldBe null
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

                    result.applicants shouldHaveSize 2
                    result.applicants.all { !it.isChecked } shouldBe true
                }
            }

            `when`("한 명만 자습체크를 했으면") {
                then("체크한 학생의 isChecked는 true, 나머지는 false다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns setOf(1L)
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute().applicants.sortedBy { it.userId }

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

                    result.applicants shouldHaveSize 2
                    result.applicants.all { it.isChecked } shouldBe true
                }
            }
        }

        given("신청자 중 금지된 학생이 있을 때") {
            val now = LocalDateTime.now(clock)
            val user1 = user(id = 1L, studentNumber = 1101)
            val user2 = user(id = 2L, studentNumber = 1102)
            val banEntity =
                StudyBanJpaEntity(
                    user = user2,
                    bannedAt = now.minusDays(1),
                    bannedUntil = now.plusDays(6),
                )

            `when`("자습 신청자 목록을 조회하면") {
                then("금지된 학생의 isBanned는 true다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        listOf(banEntity)
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { userRepository.findAllById(any()) } returns listOf(user1, user2)

                    val result = service.execute().applicants.sortedBy { it.userId }

                    result[0].isBanned shouldBe false
                    result[1].isBanned shouldBe true
                }
            }
        }

        given("금지되고 체크도 된 학생이 있을 때") {
            val now = LocalDateTime.now(clock)
            val user1 = user(id = 1L, studentNumber = 1101)
            val banEntity =
                StudyBanJpaEntity(
                    user = user1,
                    bannedAt = now.minusDays(1),
                    bannedUntil = now.plusDays(6),
                )

            `when`("자습 신청자 목록을 조회하면") {
                then("isBanned와 isChecked가 모두 true다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        listOf(banEntity)
                    every { studyRedisAdapter.getAttendanceIds() } returns setOf(1L)
                    every { userRepository.findAllById(any()) } returns listOf(user1)

                    val result = service.execute().applicants

                    result[0].isBanned shouldBe true
                    result[0].isChecked shouldBe true
                }
            }
        }

        given("성별이 다른 학생 2명이 신청했을 때") {
            val manUser = user(id = 1L, studentNumber = 1101, sex = Sex.MAN)
            val womanUser = user(id = 2L, studentNumber = 1102, sex = Sex.WOMAN)

            `when`("자습 신청자 목록을 조회하면") {
                then("sex 필드가 올바르게 매핑된다") {
                    every { studyRedisAdapter.getApplicantIds() } returns setOf(1L, 2L)
                    every { studyBanJpaRepository.findAllByUserIdInAndBannedUntilAfter(any(), any()) } returns
                        emptyList()
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { userRepository.findAllById(any()) } returns listOf(manUser, womanUser)

                    val result = service.execute().applicants.sortedBy { it.userId }

                    result[0].sex shouldBe Sex.MAN
                    result[1].sex shouldBe Sex.WOMAN
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

                    val result = service.execute().applicants

                    result[0].studentNumber shouldBe 1101
                    result[1].studentNumber shouldBe 1102
                    result[2].studentNumber shouldBe 1103
                }
            }
        }

        given("현재 사용자가 오늘 자습을 취소했을 때") {
            `when`("자습 신청자 목록을 조회하면") {
                then("내 신청 상태가 CANCELLED로 반환된다") {
                    every { studyRedisAdapter.getApplicantIds() } returns emptySet()
                    every { studyRedisAdapter.getAttendanceIds() } returns emptySet()
                    every { studyRedisAdapter.getApplicationStatus(currentUser.id) } returns
                        StudyApplicationStatus.CANCELLED

                    val result = service.execute()

                    result.myApplicationStatus shouldBe StudyApplicationStatus.CANCELLED
                }
            }
        }
    })
