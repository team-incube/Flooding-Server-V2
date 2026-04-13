package team.incube.flooding.domain.dormitory.cleaningzone.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.dormitory.cleaningzone.entity.CleaningZoneJpaEntity
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.AssignCleaningZoneMembersRequest
import team.incube.flooding.domain.dormitory.cleaningzone.repository.CleaningZoneRepository
import team.incube.flooding.domain.dormitory.cleaningzone.service.impl.AssignCleaningZoneMembersServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class AssignCleaningZoneMembersServiceTest :
    BehaviorSpec({
        val cleaningZoneRepository = mockk<CleaningZoneRepository>()
        val userRepository = mockk<UserRepository>()

        val service = AssignCleaningZoneMembersServiceImpl(cleaningZoneRepository, userRepository)

        beforeTest {
            clearMocks(cleaningZoneRepository, userRepository)
        }

        fun zone(id: Long) = CleaningZoneJpaEntity(id = id, name = "구역$id", description = "설명$id")

        fun user(id: Long) =
            UserJpaEntity(
                id = id,
                name = "유저$id",
                sex = Sex.MAN,
                email = "user$id@test.com",
                studentNumber = 1101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        given("존재하지 않는 구역 ID로 요청할 때") {
            `when`("인원 배정을 시도하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { cleaningZoneRepository.findById(999L) } returns Optional.empty()

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(999L, AssignCleaningZoneMembersRequest(userIds = listOf(1L)))
                        }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("존재하지 않는 유저 ID가 포함된 요청일 때") {
            `when`("인원 배정을 시도하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    val zone = zone(1L)
                    every { cleaningZoneRepository.findById(1L) } returns Optional.of(zone)
                    justRun { userRepository.clearCleaningZoneByZoneId(1L) }
                    every { userRepository.findAllById(listOf(1L, 2L)) } returns listOf(user(1L))

                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(1L, AssignCleaningZoneMembersRequest(userIds = listOf(1L, 2L)))
                        }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("유효한 구역과 유저 목록으로 요청할 때") {
            `when`("인원 배정을 하면") {
                then("기존 멤버가 벌크 해제되고 새 멤버가 저장된다") {
                    val zone = zone(1L)
                    val newMembers = listOf(user(3L), user(4L))
                    every { cleaningZoneRepository.findById(1L) } returns Optional.of(zone)
                    justRun { userRepository.clearCleaningZoneByZoneId(1L) }
                    every { userRepository.findAllById(listOf(3L, 4L)) } returns newMembers
                    every { userRepository.saveAll(newMembers) } returns newMembers

                    service.execute(1L, AssignCleaningZoneMembersRequest(userIds = listOf(3L, 4L)))

                    verify(exactly = 1) { userRepository.clearCleaningZoneByZoneId(1L) }
                    verify(exactly = 1) { userRepository.saveAll(newMembers) }
                    newMembers.forEach { it.cleaningZone shouldBe zone }
                }
            }
        }

        given("빈 유저 목록으로 요청할 때") {
            `when`("인원 배정을 하면") {
                then("기존 멤버가 벌크 해제되고 새 멤버 저장은 빈 목록으로 호출된다") {
                    val zone = zone(1L)
                    every { cleaningZoneRepository.findById(1L) } returns Optional.of(zone)
                    justRun { userRepository.clearCleaningZoneByZoneId(1L) }
                    every { userRepository.findAllById(emptyList()) } returns emptyList()
                    every { userRepository.saveAll(emptyList<UserJpaEntity>()) } returns emptyList()

                    service.execute(1L, AssignCleaningZoneMembersRequest(userIds = emptyList()))

                    verify(exactly = 1) { userRepository.clearCleaningZoneByZoneId(1L) }
                    verify(exactly = 1) { userRepository.saveAll(emptyList<UserJpaEntity>()) }
                }
            }
        }
    })
