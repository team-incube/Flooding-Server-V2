package team.incube.flooding.domain.homebase.dto.response

data class GetHomebaseResponse (
    val homebaseId: Long,
    val floor: Int,
    val tableId: Int,
    val isAttended: Boolean,
    val members: List<GetHomebaseMemberResponse>
)