package team.incube.flooding.domain.club.service.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.GetClubService
import team.incube.flooding.global.client.DataGsmProjectClient
import team.themoment.sdk.exception.ExpectedException

@Service
class GetClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubParticipantRepository: ClubParticipantRepository,
    private val dataGsmProjectClient: DataGsmProjectClient,
) : GetClubService {
    override fun execute(clubId: Long): GetClubResponse {
        val club =
            clubRepository.findByIdWithLeader(clubId)
                ?: throw ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)

        return runBlocking {
            val membersDeferred =
                async(Dispatchers.IO) {
                    clubParticipantRepository.findAllByClubId(clubId).map { p ->
                        GetClubResponse.MemberSummary(
                            id = p.user.id,
                            name = p.user.name,
                            studentNumber = p.user.studentNumber,
                            sex = p.user.sex.name,
                            specialty = p.user.specialty,
                        )
                    }
                }
            val projectsDeferred =
                async(Dispatchers.IO) {
                    dataGsmProjectClient.getProjectsByClubId(clubId)
                }

            GetClubResponse(
                club =
                    GetClubResponse.ClubDetail(
                        id = club.id,
                        name = club.name,
                        type = club.type.name,
                        leader = club.leader?.name,
                        description = club.description,
                        imageUrl = club.imageUrl,
                        maxMember = club.maxMember,
                    ),
                member = membersDeferred.await(),
                project = projectsDeferred.await(),
            )
        }
    }
}
