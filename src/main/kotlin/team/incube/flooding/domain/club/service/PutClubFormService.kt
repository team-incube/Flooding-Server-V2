package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.PutClubFormRequest

interface PutClubFormService {
    fun execute(
        clubId: Long,
        request: PutClubFormRequest,
    )
}
