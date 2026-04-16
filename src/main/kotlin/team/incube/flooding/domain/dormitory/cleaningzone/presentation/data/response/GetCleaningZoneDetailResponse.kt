package team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response

data class GetCleaningZoneDetailResponse(
    val id: Long,
    val name: String,
    val description: String,
    val members: List<CleaningZoneMemberDto>,
)
