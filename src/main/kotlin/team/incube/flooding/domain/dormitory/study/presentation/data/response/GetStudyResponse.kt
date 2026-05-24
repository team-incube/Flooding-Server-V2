package team.incube.flooding.domain.dormitory.study.presentation.data.response

import team.incube.flooding.domain.user.entity.Sex

data class GetStudyResponse(
    val userId: Long,
    val name: String,
    val studentNumber: Int,
    val sex: Sex,
    val isBanned: Boolean,
    val isChecked: Boolean,
)
