package team.incube.flooding.domain.homebase.dto.request

import team.incube.flooding.domain.homebase.dto.MemberDto

data class CreateHomebaseRequest(
    val startPeriod: Int,
    val endPeriod: Int,
    val members: List<MemberDto>
)