package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse

interface GetClubService {
    suspend fun execute(clubId: Long): GetClubResponse
}
