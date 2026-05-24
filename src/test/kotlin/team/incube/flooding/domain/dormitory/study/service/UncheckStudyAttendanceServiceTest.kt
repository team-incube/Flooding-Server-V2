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
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.service.impl.UncheckStudyAttendanceServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class UncheckStudyAttendanceServiceTest :
    BehaviorSpec({
        val userRepository = mockk<UserRepository>()
        val studyRedisAdapter = mockk<StudyRedisAdapter>()
        val sseEmitterRegistry = mockk<StudyAttendanceSseEmitterRegistry>()
        val service = UncheckStudyAttendanceServiceImpl(userRepository, studyRedisAdapter, sseEmitterRegistry)

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

        given("존재하지 않는 학생 ID로 요청할 때") {
            `when`("체크인을 해제하면") {
                then("NOT_FOUND 예외 - 존재하지 않는 학생") {
                    every { userRepository.findById(1L) } returns Optional.empty()

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }

                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    exception.message shouldBe "존재하지 않는 학생입니다."
                }
            }
        }

        given("학생은 존재하지만 체크인 기록이 없을 때") {
            `when`("체크인을 해제하면") {
                then("NOT_FOUND 예외 - 체크인 기록 없음") {
                    every { userRepository.findById(1L) } returns Optional.of(user(1L))
                    every { studyRedisAdapter.isAttendanceChecked(1L) } returns false

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }

                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    exception.message shouldBe "체크인 기록이 없습니다."
                }
            }
        }

        given("체크인된 학생이 있을 때") {
            `when`("체크인을 해제하면") {
                then("Redis에서 체크인을 삭제하고 SSE cancel-attendance 이벤트를 브로드캐스트한다") {
                    val targetUser = user(1L)
                    every { userRepository.findById(1L) } returns Optional.of(targetUser)
                    every { studyRedisAdapter.isAttendanceChecked(1L) } returns true
                    justRun { studyRedisAdapter.cancelAttendance(1L) }
                    justRun {
                        sseEmitterRegistry.broadcastCancel(
                            StudyAttendanceEventResponse(
                                userId = targetUser.id,
                                name = targetUser.name,
                                studentNumber = targetUser.studentNumber,
                            ),
                        )
                    }

                    service.execute(1L)

                    verify(exactly = 1) { studyRedisAdapter.cancelAttendance(1L) }
                    verify(exactly = 1) {
                        sseEmitterRegistry.broadcastCancel(
                            StudyAttendanceEventResponse(
                                userId = targetUser.id,
                                name = targetUser.name,
                                studentNumber = targetUser.studentNumber,
                            ),
                        )
                    }
                }
            }
        }
    })
