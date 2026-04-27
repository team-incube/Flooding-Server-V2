package team.incube.flooding.domain.club.dto.response

import team.incube.flooding.domain.club.entity.ClubType

data class ClubApplicationListResponse(
    val clubs: List<ClubApplicationResponse>,
)

data class ClubApplicationResponse(
    val id: Long,
    val name: String,
    val leader: String,
    val type: ClubType,
    val description: String,
    val imageUrl: String?,
    val maxMember: Int?,
)
