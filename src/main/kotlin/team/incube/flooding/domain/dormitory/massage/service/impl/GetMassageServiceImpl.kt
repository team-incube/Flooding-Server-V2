package team.incube.flooding.domain.dormitory.massage.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse
import team.incube.flooding.domain.dormitory.massage.service.GetMassageService
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class GetMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val currentUserProvider: CurrentUserProvider,
) : GetMassageService {
    override fun execute(): GetMassageResponse {
        val user = currentUserProvider.getCurrentUser()
        val isApplied = massageRedisAdapter.isApply(user.id)
        val order = if (isApplied) massageRedisAdapter.getOrder(user.id) else null
        return GetMassageResponse(
            isApplied = isApplied,
            order = order,
            currentCount = massageRedisAdapter.getCount(),
        )
    }
}
