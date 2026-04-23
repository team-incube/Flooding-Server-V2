package team.incube.flooding.domain.dormitory.massage.service

import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse

interface GetMassageService {
    fun execute(): List<GetMassageResponse>
}
