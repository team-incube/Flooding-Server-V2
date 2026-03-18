package team.incube.flooding.domain.homebase.dto

data class CreateHomebaseRequest(
    val homebaseId: Long,
    val startPeriod: Int,
    val endPeriod: Int,
    val members: List<MemberDto>
)