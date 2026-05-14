package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.GetClubService
import team.incube.flooding.global.client.DataGsmProjectClient
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class GetClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubParticipantRepository: ClubParticipantRepository,
    private val dataGsmProjectClient: DataGsmProjectClient,
    private val currentUserProvider: CurrentUserProvider,
) : GetClubService {
    override fun execute(clubId: Long): GetClubResponse {
        val currentUser = currentUserProvider.getCurrentUser()
        val club =
            clubRepository.findByIdWithLeader(clubId)
                ?: throw ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
        val participantMembers =
            clubParticipantRepository
                .findAllByClubId(clubId)
                .map { p -> p.user }
        val members =
            (listOfNotNull(club.leader) + participantMembers)
                .distinctBy { it.id }
                .map { user ->
                    GetClubResponse.MemberSummary(
                        id = user.id,
                        name = user.name,
                        studentNumber = user.studentNumber,
                        sex = user.sex.name,
                        specialty = user.specialty,
                    )
                }
        val projects =
            club.dataGsmClubId?.let { dgId ->
                runCatching { dataGsmProjectClient.getProjectsByClubId(dgId) }.getOrElse { emptyList() }
            } ?: emptyList()

        return GetClubResponse(
            club =
                GetClubResponse.ClubDetail(
                    id = club.id,
                    name = club.name,
                    type = club.type.name,
                    leaderId = club.leader?.id,
                    leader = club.leader?.name,
                    description = club.description,
                    imageUrl = club.imageUrl,
                    maxMember = club.maxMember,
                ),
            members = members,
            projects = projects,
            isLeader = club.leader?.id == currentUser.id,
        )
    }
}
