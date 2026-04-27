package team.incube.flooding.domain.user.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse
import team.incube.flooding.domain.user.service.GetMeService
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class GetMeServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : GetMeService {
    @Transactional(readOnly = true)
    override fun execute(): GetMeResponse {
        val user = currentUserProvider.getCurrentUser()
        return GetMeResponse.from(user)
    }
}
