package team.incube.flooding.domain.dormitory.study.presentation.data.response

import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus

data class GetStudyListResponse(
    val isApplicationOpen: Boolean,
    val myApplicationStatus: StudyApplicationStatus?,
    val applicants: List<GetStudyResponse>,
)
