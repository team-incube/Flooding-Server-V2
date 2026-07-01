package team.incube.flooding.domain.dormitory.study.service

import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyAttendanceListResponse

interface GetPublicStudyAttendanceListService {
    fun execute(): List<GetStudyAttendanceListResponse>
}
