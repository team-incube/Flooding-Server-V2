package team.incube.flooding.domain.dormitory.penalty.service

import team.incube.flooding.domain.dormitory.penalty.presentation.data.request.SetPenaltyRequest

interface SetPenaltyService {
    fun execute(
        userId: Long,
        request: SetPenaltyRequest,
    )
}
