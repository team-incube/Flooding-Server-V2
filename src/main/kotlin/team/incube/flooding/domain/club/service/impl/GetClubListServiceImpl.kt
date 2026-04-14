package team.incube.flooding.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.GetClubListService

@Service
@Transactional(readOnly = true)
class GetClubListServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubParticipantRepository: ClubParticipantRepository,
) : GetClubListService {
    override fun execute(
        type: ClubType,
        name: String?,
    ): GetClubListResponse {
        val clubs =
            if (name.isNullOrBlank()) {
                clubRepository.findAllByType(type)
            } else {
                clubRepository.findAllByTypeAndKeyword(type, name.trim())
            }
        if (clubs.isEmpty()) {
            return GetClubListResponse(clubs = emptyList())
        }
        val countMap =
            clubParticipantRepository
                .countGroupByClubIdIn(clubs.map { it.id })
                .associate { it.clubId to it.count }
        return GetClubListResponse(
            clubs =
                clubs.map { club ->
                    GetClubListResponse.ClubSummary(
                        id = club.id,
                        name = club.name,
                        type = club.type.name,
                        description = club.description,
                        imageUrl = club.imageUrl,
                        totalMember = countMap[club.id] ?: 0L,
                    )
                },
        )
    }
}
