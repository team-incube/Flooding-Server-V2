package team.incube.flooding.domain.dormitory.study.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.impl.UnbanStudyServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UnbanStudyServiceTest :
    BehaviorSpec({
        val userRepository = mockk<UserRepository>()
        val studyBanJpaRepository = mockk<StudyBanJpaRepository>()
        val clock = Clock.fixed(Instant.parse("2026-04-29T12:00:00Z"), ZoneId.of("Asia/Seoul"))
        val service = UnbanStudyServiceImpl(userRepository, studyBanJpaRepository, clock)

        beforeEach { clearAllMocks() }

        fun user(id: Long) =
            UserJpaEntity(
                id = id,
                name = "학생$id",
                sex = Sex.MAN,
                email = "student$id@test.com",
                studentNumber = 1101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        given("활성 자습 금지 내역이 없고 유저도 존재하지 않을 때") {
            `when`("자습 금지를 해제하면") {
                then("NOT_FOUND 예외 - 존재하지 않는 유저") {
                    val now = LocalDateTime.now(clock)
                    every { studyBanJpaRepository.findByUserIdAndBannedUntilAfter(1L, now) } returns null
                    every { userRepository.existsById(1L) } returns false

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }

                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    exception.message shouldBe "존재하지 않는 유저입니다."
                }
            }
        }

        given("학생은 존재하지만 활성 자습 금지 내역이 없을 때") {
            `when`("자습 금지를 해제하면") {
                then("NOT_FOUND 예외 - 자습 금지 내역 없음") {
                    val now = LocalDateTime.now(clock)
                    every { studyBanJpaRepository.findByUserIdAndBannedUntilAfter(1L, now) } returns null
                    every { userRepository.existsById(1L) } returns true

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }

                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    exception.message shouldBe "자습 금지 내역이 없습니다."
                }
            }
        }

        given("활성 자습 금지 상태인 학생이 있을 때") {
            `when`("자습 금지를 해제하면") {
                then("자습 금지 내역을 삭제한다") {
                    val targetUser = user(1L)
                    val now = LocalDateTime.now(clock)
                    val studyBan =
                        StudyBanJpaEntity(
                            id = 1L,
                            user = targetUser,
                            bannedAt = now.minusDays(1),
                            bannedUntil = now.plusDays(6),
                        )
                    every { studyBanJpaRepository.findByUserIdAndBannedUntilAfter(1L, now) } returns studyBan
                    justRun { studyBanJpaRepository.delete(studyBan) }

                    service.execute(1L)

                    verify(exactly = 1) { studyBanJpaRepository.delete(studyBan) }
                }
            }
        }
    })
