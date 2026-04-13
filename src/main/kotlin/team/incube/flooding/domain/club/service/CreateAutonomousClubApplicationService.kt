package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse

interface CreateAutonomousClubApplicationService {
    fun execute(clubId: Long): CreateAutonomousClubApplicationResponse
}
