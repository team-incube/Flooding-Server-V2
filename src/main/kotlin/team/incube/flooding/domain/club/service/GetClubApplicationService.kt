package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationResponse

interface GetClubApplicationService {
    fun execute(clubId: Long): GetClubApplicationResponse
}
