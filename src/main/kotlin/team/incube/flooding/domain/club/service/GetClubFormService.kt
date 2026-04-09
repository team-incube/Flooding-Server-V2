package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.response.GetClubFormResponse

interface GetClubFormService {
    fun execute(clubId: Long): GetClubFormResponse
}
