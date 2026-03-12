package team.incube.flooding.domain.homebase.dto.response

import team.incube.flooding.domain.homebase.dto.MemberDto

data class GetHomebaseResponse (
    val homebaseId: Long,
    val floor: Int,
    val tableNumber: Int,
    val isAttended: Boolean,
    val members: List<MemberDto>
)