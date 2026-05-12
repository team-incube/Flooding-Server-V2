package team.incube.flooding.domain.dormitory.study.service.impl

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.StudyApplicationService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Service
@Transactional
class StudyApplicationServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val studyProperties: StudyProperties,
    private val redissonClient: RedissonClient,
    private val clock: Clock,
) : StudyApplicationService {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute() {
        val user = currentUserProvider.getCurrentUser()

        val now = LocalTime.now(clock)

        val crossesMidnight = studyProperties.openTime.isAfter(studyProperties.closeTime)
        val outOfRange =
            if (crossesMidnight) {
                now.isBefore(studyProperties.openTime) && now.isAfter(studyProperties.closeTime)
            } else {
                now.isBefore(studyProperties.openTime) || now.isAfter(studyProperties.closeTime)
            }

//        if (outOfRange) {
//            log.warn("자습 신청 가능 시간 아님: userId={}, now={}, openTime={}, closeTime={}", user.id, now, studyProperties.openTime, studyProperties.closeTime)
//            throw ExpectedException("자습 신청 시간이 아닙니다.", HttpStatus.BAD_REQUEST)
//        }

        if (studyRedisAdapter.getApplicationStatus(user.id) == StudyApplicationStatus.BANNED ||
            studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(user.id, LocalDateTime.now(clock))
        ) {
            log.warn("자습 신청 거절 - 금지 상태: userId={}", user.id)
            throw ExpectedException("자습 금지 상태입니다.", HttpStatus.FORBIDDEN)
        }

        val lock = redissonClient.getLock(studyProperties.lockKey)
        val acquired = lock.tryLock(5, 3, TimeUnit.SECONDS)

        if (!acquired) {
            log.warn("자습 신청 거절 - 락 획득 실패: userId={}", user.id)
            throw ExpectedException("잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS)
        }

        try {
            val status = studyRedisAdapter.getApplicationStatus(user.id)
            if (status == StudyApplicationStatus.APPROVED) {
                log.warn("자습 신청 거절 - 이미 신청됨: userId={}, status={}", user.id, status)
                throw ExpectedException("이미 자습을 신청했습니다.", HttpStatus.CONFLICT)
            }
            if (status == StudyApplicationStatus.CANCELLED) {
                log.warn("자습 신청 거절 - 당일 취소 이력: userId={}, status={}", user.id, status)
                throw ExpectedException("자습을 취소하여 신청하지 못합니다.", HttpStatus.CONFLICT)
            }

            val newCount = studyRedisAdapter.incrementCount()
            if (newCount > studyProperties.maxCount) {
                studyRedisAdapter.decrementCount()
                log.warn(
                    "자습 신청 거절 - 정원 초과: userId={}, currentCount={}, maxCount={}",
                    user.id,
                    newCount,
                    studyProperties.maxCount,
                )
                throw ExpectedException("자습 신청 인원이 마감되었습니다.", HttpStatus.CONFLICT)
            }

            studyRedisAdapter.saveApplication(user.id, StudyApplicationStatus.APPROVED)
            studyRedisAdapter.addApplicant(user.id)
            log.info(
                "자습 신청 완료: userId={}, count={}, maxCount={}, appliedAt={}",
                user.id,
                newCount,
                studyProperties.maxCount,
                LocalDateTime.now(clock),
            )
        } finally {
            lock.unlock()
        }
    }
}
