package team.incube.flooding.domain.dormitory.penalty.presentation.data.response

data class GetPenaltyResponse(
    val userId: Long,
    val name: String,
    val studentNumber: Int,
    val penaltyScore: Int,
)
