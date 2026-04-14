package team.incube.flooding.domain.club.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.CreateClubServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider

class CreateClubServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()

        val service = CreateClubServiceImpl(clubRepository, currentUserProvider)

        beforeEach { clearAllMocks() }

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

        given("status=NEW로 요청이 올 때") {
            `when`("개설 신청하면") {
                then("status가 NEW인 동아리가 저장된다") {
                    val request =
                        CreateClubRequest(
                            name = "테스트 동아리",
                            type = ClubType.MAJOR_CLUB,
                            status = ClubStatus.NEW,
                            description = "테스트 설명",
                            imageUrl = null,
                            maxMember = 20,
                        )
                    val slot = slot<ClubJpaEntity>()
                    every { currentUserProvider.getCurrentUser() } returns leader
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

        given("status=MAINTAIN으로 요청이 올 때") {
            `when`("개설 신청하면") {
                then("status가 MAINTAIN인 동아리가 저장된다") {
                    val request =
                        CreateClubRequest(
                            name = "테스트 동아리",
                            type = ClubType.MAJOR_CLUB,
                            status = ClubStatus.MAINTAIN,
                            description = "테스트 설명",
                            imageUrl = null,
                            maxMember = 20,
                        )
                    val slot = slot<ClubJpaEntity>()
                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { clubRepository.save(capture(slot)) } answers { slot.captured }

                    service.execute(request)

                    val saved = slot.captured
                    saved.status shouldBe ClubStatus.MAINTAIN
                    verify(exactly = 1) { clubRepository.save(any()) }
                }
            }
        }
    })
