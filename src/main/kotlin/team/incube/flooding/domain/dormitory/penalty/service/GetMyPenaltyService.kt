package team.incube.flooding.domain.dormitory.penalty.service

import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetMyPenaltyResponse

interface GetMyPenaltyService {
    fun execute(): GetMyPenaltyResponse
}
