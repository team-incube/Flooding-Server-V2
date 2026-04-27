package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.PutClubRequest

interface PutClubService {
    fun execute(
        clubId: Long,
        request: PutClubRequest,
    )
}
