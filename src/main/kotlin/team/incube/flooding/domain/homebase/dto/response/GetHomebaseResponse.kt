package team.incube.flooding.domain.homebase.dto.response

import team.incube.flooding.domain.homebase.dto.MemberDto

data class GetHomebaseResponse(
    val id: Long,
    val startPeriod: Int,
    val endPeriod: Int,
    val homebaseId: Long,
    val members: List<MemberDto>,
)
