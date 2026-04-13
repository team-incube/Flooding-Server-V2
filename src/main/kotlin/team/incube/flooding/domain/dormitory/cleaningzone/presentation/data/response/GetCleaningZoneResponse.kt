package team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response

data class GetCleaningZoneResponse(
    val id: Long,
    val name: String,
    val description: String,
    val memberCount: Int,
)
