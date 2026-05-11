package team.incube.flooding.domain.dormitory.massage.service

import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageListResponse

interface GetMassageService {
    fun execute(): GetMassageListResponse
}
