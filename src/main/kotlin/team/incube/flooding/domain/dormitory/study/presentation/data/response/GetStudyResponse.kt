package team.incube.flooding.domain.dormitory.study.presentation.data.response

import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus

data class GetStudyResponse(
    val status: StudyApplicationStatus?,
    val currentCount: Long,
    val isBanned: Boolean,
)
