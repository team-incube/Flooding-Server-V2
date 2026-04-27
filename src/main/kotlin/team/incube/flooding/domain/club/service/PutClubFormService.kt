package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.CreateClubFormRequest

interface PutClubFormService {
    fun execute(
        clubId: Long,
        request: CreateClubFormRequest,
    )
}
