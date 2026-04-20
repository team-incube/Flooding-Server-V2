package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationListResponse

interface GetClubApplicationListService {
    fun execute(clubId: Long): GetClubApplicationListResponse
}
