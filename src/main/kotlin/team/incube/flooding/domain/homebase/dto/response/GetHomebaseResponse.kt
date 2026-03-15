package team.incube.flooding.domain.homebase.dto.response

import team.incube.flooding.domain.homebase.dto.MemberDto

data class GetHomebaseResponse (
    val homebaseId: Long,
    val period: Int,
    val members: List<MemberDto>
)