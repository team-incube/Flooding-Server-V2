package team.incube.flooding.domain.dormitory.penalty.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetPenaltyResponse

interface GetAllPenaltyService {
    fun execute(pageable: Pageable): Page<GetPenaltyResponse>
}
