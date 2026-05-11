package team.incube.flooding.domain.dormitory.study.service

import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyListResponse

interface GetStudyService {
    fun execute(): GetStudyListResponse
}
