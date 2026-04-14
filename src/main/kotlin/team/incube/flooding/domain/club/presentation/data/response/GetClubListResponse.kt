package team.incube.flooding.domain.club.presentation.data.response

data class GetClubListResponse(
    val clubs: List<ClubSummary>,
) {
    data class ClubSummary(
        val id: Long,
        val name: String,
        val type: String,
        val description: String?,
        val imageUrl: String?,
        val totalMember: Long,
    )
}
