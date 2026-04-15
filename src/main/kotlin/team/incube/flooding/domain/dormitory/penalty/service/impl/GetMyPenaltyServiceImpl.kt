package team.incube.flooding.domain.dormitory.penalty.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetMyPenaltyResponse
import team.incube.flooding.domain.dormitory.penalty.service.GetMyPenaltyService
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class GetMyPenaltyServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : GetMyPenaltyService {
    @Transactional(readOnly = true)
    override fun execute(): GetMyPenaltyResponse {
        val user = currentUserProvider.getCurrentUser()
        return GetMyPenaltyResponse(penaltyScore = user.penaltyScore)
    }
}
