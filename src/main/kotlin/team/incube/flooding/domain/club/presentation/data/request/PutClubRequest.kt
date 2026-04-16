package team.incube.flooding.domain.club.presentation.data.request

data class PutClubRequest(
    val name: String,
    val description: String,
    val imageUrl: String?,
    val maxMember: Int?,
)
