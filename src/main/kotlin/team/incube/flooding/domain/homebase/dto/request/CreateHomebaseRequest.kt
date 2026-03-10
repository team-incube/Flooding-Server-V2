package team.incube.flooding.domain.homebase.dto.request

data class CreateHomebaseRequest (
    val period: Int,
    val floor: Int,
    val tableNumber: Int,
    val members: List<CreateHomebaseMemberRequest>
)