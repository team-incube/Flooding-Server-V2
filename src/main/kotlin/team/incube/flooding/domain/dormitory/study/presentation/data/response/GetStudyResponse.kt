package team.incube.flooding.domain.dormitory.study.presentation.data.response

data class GetStudyResponse(
    val userId: Long,
    val name: String,
    val studentNumber: Int,
    val isBanned: Boolean,
    val isChecked: Boolean,
)
