package team.incube.flooding.domain.dormitory.massage.presentation.data.response

data class GetMassageResponse(
    val isApplied: Boolean,
    val order: Long?,
    val currentCount: Long,
)
