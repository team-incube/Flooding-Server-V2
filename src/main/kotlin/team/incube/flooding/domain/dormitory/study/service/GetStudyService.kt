package team.incube.flooding.domain.dormitory.study.service

import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyResponse

interface GetStudyService {
    fun execute(): GetStudyResponse
}
