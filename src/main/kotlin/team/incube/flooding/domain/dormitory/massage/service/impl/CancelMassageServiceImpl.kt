package team.incube.flooding.domain.dormitory.massage.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.service.CancelMassageService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class CancelMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val currentUserProvider: CurrentUserProvider,
) : CancelMassageService {
    override fun execute() {
        val user = currentUserProvider.getCurrentUser()

        if (!massageRedisAdapter.isApply(user.id)) {
            throw ExpectedException("안마의자 신청 내역이 없습니다.", HttpStatus.NOT_FOUND)
        }

        massageRedisAdapter.cancel(user.id)
    }
}
