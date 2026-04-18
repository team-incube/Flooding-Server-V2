package team.incube.flooding.domain.club.presentation.data.response

data class GetClubResponse(
    val club: ClubDetail,
    val members: List<MemberSummary>,
    val projects: List<ProjectSummary>,
) {
    data class ClubDetail(
        val id: Long,
        val name: String,
        val type: String,
        val leader: String?,
        val description: String?,
        val imageUrl: String?,
        val maxMember: Int?,
    )

    data class MemberSummary(
        val id: Long,
        val name: String,
        val studentNumber: Int,
        val sex: String,
        val specialty: String?,
    )

    data class ProjectSummary(
        val id: Long,
        val name: String,
        val description: String,
        val participants: List<ParticipantSummary>,
    )

    data class ParticipantSummary(
        val id: Long,
        val name: String,
        val studentNumber: Int?,
        val sex: String,
        val specialty: String?,
    )
}
