package team.incube.flooding.domain.homebase.dto

data class HomebaseRequest (

    val period: Int,
    val floor: Int,
    val tableId: Int,
    val members: List<MemberRequest>
)