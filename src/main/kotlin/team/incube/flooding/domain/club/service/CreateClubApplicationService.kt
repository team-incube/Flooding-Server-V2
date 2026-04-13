package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubApplicationResponse

interface CreateClubApplicationService {
    fun execute(
        clubId: Long,
        request: CreateClubApplicationRequest,
    ): CreateClubApplicationResponse
}
