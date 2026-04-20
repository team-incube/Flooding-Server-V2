package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.dto.response.ClubApplicationListResponse

interface QueryClubApplicationService {
    fun execute(): ClubApplicationListResponse
}
