package team.incube.flooding.domain.club.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.GetClubApplicationListServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider

class GetClubApplicationListServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val clubFormRepository = mockk<ClubFormRepository>()
        val submissionRepository = mockk<ClubFormSubmissionRepository>()
        val answerRepository = mockk<ClubFormAnswerRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service =
            GetClubApplicationListServiceImpl(
                clubRepository,
                clubFormRepository,
                submissionRepository,
                answerRepository,
                currentUserProvider,
            )

        beforeEach { clearAllMocks() }

        fun user(id: Long) =
            UserJpaEntity(
                id = id,
                name = "테스트$id",
                sex = Sex.MAN,
                email = "test$id@test.com",
                studentNumber = 10100 + id.toInt(),
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        fun club(leader: UserJpaEntity) =
            ClubJpaEntity(
                id = 1L,
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = leader,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = null,
                maxMember = null,
            )

        given("동아리 신청 목록을 조회할 때") {
            `when`("활성 폼이 없으면") {
                then("null 대신 빈 리스트를 반환한다") {
                    val leader = user(1L)
                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { clubRepository.findByIdWithLeader(1L) } returns club(leader)
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns null

                    val response = service.execute(1L)

                    response.applications.shouldBeEmpty()
                }
            }

            `when`("신청자가 없으면") {
                then("빈 리스트를 반환한다") {
                    val leader = user(1L)
                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { clubRepository.findByIdWithLeader(1L) } returns club(leader)
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns
                        ClubFormJpaEntity(
                            id = 10L,
                            club = club(leader),
                            title = "신청 폼",
                            description = null,
                        )
                    every { submissionRepository.findAllByFormIdWithUser(10L) } returns emptyList()

                    val response = service.execute(1L)

                    response.applications shouldBe emptyList()
                }
            }
        }
    })
