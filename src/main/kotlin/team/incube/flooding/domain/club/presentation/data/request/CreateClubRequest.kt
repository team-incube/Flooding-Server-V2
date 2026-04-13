package team.incube.flooding.domain.club.presentation.data.request

import team.incube.flooding.domain.club.entity.ClubType

data class CreateClubRequest(
    val name: String,
    val type: ClubType,
    val description: String,
    val imageUrl: String?,
    val leaderId: Long,
    val maxMember: Int?,
)
