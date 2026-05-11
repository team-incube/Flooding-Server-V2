package team.incube.flooding.domain.dormitory.massage.adapter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class MassageRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val clock: Clock,
) {
    companion object {
        private const val QUEUE_KEY = "massage:queue"
        private const val CANCELLED_KEY_PREFIX = "massage:cancelled:"
    }

    private fun ttlUntilMidnight(): Duration {
        val midnight = LocalDateTime.of(LocalDate.now(clock).plusDays(1), LocalTime.MIDNIGHT)
        return Duration.between(LocalDateTime.now(clock), midnight)
    }

    private fun cancelledKey(userId: Long): String = "$CANCELLED_KEY_PREFIX$userId"

    fun apply(userId: Long): Long {
        val size = redisTemplate.opsForList().rightPush(QUEUE_KEY, userId.toString())
        if (size == 1L) {
            redisTemplate.expire(QUEUE_KEY, ttlUntilMidnight())
        }
        return size
    }

    fun cancel(userId: Long) {
        redisTemplate.opsForList().remove(QUEUE_KEY, 1, userId.toString())
    }

    fun markCancelledToday(userId: Long) {
        redisTemplate.opsForValue().set(cancelledKey(userId), "true", ttlUntilMidnight())
    }

    fun isReapplyBlocked(userId: Long): Boolean = redisTemplate.hasKey(cancelledKey(userId))

    fun isApply(userId: Long): Boolean =
        redisTemplate
            .opsForList()
            .range(QUEUE_KEY, 0, -1)
            ?.contains(userId.toString()) ?: false

    fun getOrder(userId: Long): Long {
        val queue = redisTemplate.opsForList().range(QUEUE_KEY, 0, -1) ?: return -1
        val index = queue.indexOf(userId.toString())
        return if (index == -1) -1 else index + 1L
    }

    fun getCount(): Long = redisTemplate.opsForList().size(QUEUE_KEY) ?: 0L

    fun getQueue(): List<Long> =
        redisTemplate
            .opsForList()
            .range(QUEUE_KEY, 0, -1)
            ?.map { it.toLong() } ?: emptyList()
}
