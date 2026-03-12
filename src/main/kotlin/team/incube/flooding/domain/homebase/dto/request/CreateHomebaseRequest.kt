package team.incube.flooding.domain.homebase.dto.request

import team.incube.flooding.domain.homebase.dto.MemberDto

data class CreateHomebaseRequest (
    val period: Int,
    val floor: Int,
    val tableNumber: Int,
    val members: List<MemberDto>
)