package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.CreateClubServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class CreateClubServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val userRepository = mockk<UserRepository>()

        val service = CreateClubServiceImpl(clubRepository, userRepository)

        val leader =
            UserJpaEntity(
                id = 1L,
                name = "리더",
                sex = Sex.MAN,
                email = "leader@test.com",
                studentNumber = 10101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        val request =
            CreateClubRequest(
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                description = "테스트 설명",
                imageUrl = null,
                leaderId = 1L,
                maxMember = 20,
            )

        given("존재하지 않는 leaderId로 요청할 때") {
            `when`("개설 신청하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { userRepository.findById(1L) } returns Optional.empty()

                    val exception = shouldThrow<ExpectedException> { service.execute(request) }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("유효한 요청이 올 때") {
            `when`("개설 신청하면") {
                then("status가 NEW인 동아리가 저장된다") {
                    val slot = slot<ClubJpaEntity>()
                    every { userRepository.findById(1L) } returns Optional.of(leader)
                    every { clubRepository.save(capture(slot)) } answers { slot.captured }

                    service.execute(request)

                    val saved = slot.captured
                    saved.name shouldBe "테스트 동아리"
                    saved.type shouldBe ClubType.MAJOR_CLUB
                    saved.status shouldBe ClubStatus.NEW
                    saved.leader shouldBe leader
                    saved.maxMember shouldBe 20
                    verify(exactly = 1) { clubRepository.save(any()) }
                }
            }
        }
    })
