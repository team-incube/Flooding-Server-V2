package team.incube.flooding.domain.dormitory.massage.presentation.data.response

data class GetMassageListResponse(
    val isApplicationOpen: Boolean,
    val applicants: List<GetMassageResponse>,
)
