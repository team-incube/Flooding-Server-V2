package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest

interface CreateClubService {
    fun execute(request: CreateClubRequest)
}
