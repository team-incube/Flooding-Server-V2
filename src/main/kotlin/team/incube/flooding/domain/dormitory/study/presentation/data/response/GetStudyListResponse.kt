package team.incube.flooding.domain.dormitory.study.presentation.data.response

data class GetStudyListResponse(
    val isApplicationOpen: Boolean,
    val applicants: List<GetStudyResponse>,
)
