package team.incube.flooding.domain.club.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.GetClubServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.client.DataGsmProjectClient
import team.incube.flooding.global.security.util.CurrentUserProvider

class GetClubServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val clubParticipantRepository = mockk<ClubParticipantRepository>()
        val dataGsmProjectClient = mockk<DataGsmProjectClient>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service =
            GetClubServiceImpl(
                clubRepository,
                clubParticipantRepository,
                dataGsmProjectClient,
                currentUserProvider,
            )

        beforeEach { clearAllMocks() }

        fun user(
            id: Long,
            name: String,
        ) = UserJpaEntity(
            id = id,
            name = name,
            sex = Sex.MAN,
            email = "test$id@test.com",
            studentNumber = 10100 + id.toInt(),
            role = Role.GENERAL_STUDENT,
            dormitoryRoom = 101,
        )

        given("동아리 상세를 조회할 때") {
            `when`("부장이 참가자 목록에 따로 저장되어 있지 않으면") {
                then("members에 부장을 포함한다") {
                    val leader = user(1L, "부장")
                    val member = user(2L, "멤버")
                    val club =
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

                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { clubRepository.findByIdWithLeader(1L) } returns club
                    every { clubParticipantRepository.findAllByClubId(1L) } returns
                        listOf(ClubParticipantJpaEntity(club = club, user = member))

                    val response = service.execute(1L)

                    response.members shouldContain
                        GetClubResponse.MemberSummary(
                            id = leader.id,
                            name = leader.name,
                            studentNumber = leader.studentNumber,
                            sex = leader.sex.name,
                            specialty = leader.specialty,
                        )
                }
            }
        }
    })
