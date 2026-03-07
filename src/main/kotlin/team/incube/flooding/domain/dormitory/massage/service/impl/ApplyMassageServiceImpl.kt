package team.incube.flooding.domain.dormitory.massage.service.impl

import org.redisson.api.RedissonClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.config.MassageProperties
import team.incube.flooding.domain.dormitory.massage.service.ApplyMassageService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Service
@Transactional
class ApplyMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val massageProperties: MassageProperties,
    private val currentUserProvider: CurrentUserProvider,
    private val redissonClient: RedissonClient,
) : ApplyMassageService {
    override fun execute() {
        val user = currentUserProvider.getCurrentUser()

        val now = LocalTime.now()

        if (now.isBefore(massageProperties.openTime)) {
            throw ExpectedException("안마의자 신청 시간이 아닙니다.", HttpStatus.BAD_REQUEST)
        }

        val lock = redissonClient.getLock(massageProperties.lockKey)
        val acquired = lock.tryLock(5, 3, TimeUnit.SECONDS)

        if (!acquired) {
            throw ExpectedException("잠시 후 다시 시작해주세요", HttpStatus.TOO_MANY_REQUESTS)
        }
        try {
            if (massageRedisAdapter.isApply(user.id)) {
                throw ExpectedException("이미 신청하였습니다.", HttpStatus.CONFLICT)
            }

            if (massageRedisAdapter.getCount() >= massageProperties.maxCount) {
                throw ExpectedException("신청 인원이 마감되었습니다.", HttpStatus.CONFLICT)
            }

            massageRedisAdapter.apply(user.id)
        } finally {
            lock.unlock()
        }
    }
}
