package team.incube.flooding.domain.homebase.dto.Response

data class GetHomebaseResponse (
    val homebaseId: Long,
    val floor: Int,
    val tableNumber: Int,
    val isAttended: Boolean,
    val members: List<GetHomebaseMemberResponse>
)