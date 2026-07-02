package team.incube.flooding.domain.dormitory.study.presentation.data.response

import java.time.LocalDate

data class GetStudyAttendanceListResponse(
    val date: LocalDate,
    val students: List<String>,
)
