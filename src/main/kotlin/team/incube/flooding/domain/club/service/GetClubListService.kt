package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse

interface GetClubListService {
    fun execute(
        type: ClubType,
        name: String?,
    ): GetClubListResponse
}
