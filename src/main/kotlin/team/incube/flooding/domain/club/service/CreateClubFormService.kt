package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.CreateClubFormRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubFormResponse

interface CreateClubFormService {
    fun execute(request: CreateClubFormRequest): CreateClubFormResponse
}
