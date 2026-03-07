package team.incube.flooding.domain.dormitory.massage.adapter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Component
class MassageRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val QUEUE_KEY = "massage:queue"
    }

    private fun ttlUntilMidnight() : Duration {
        val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
        return Duration.between(LocalDateTime.now(), midnight)
    }

    fun apply(userId : Long) : Long {
        val size = redisTemplate.opsForList().rightPush(QUEUE_KEY,userId.toString())
        if (size == 1L) {
            redisTemplate.expire(QUEUE_KEY, ttlUntilMidnight())
        }
        return size
    }

    fun cancel(userId : Long) {
        redisTemplate.opsForList().remove(QUEUE_KEY,1,userId.toString())
    }

    fun isApply(userId : Long) : Boolean {
        return redisTemplate.opsForList().range(QUEUE_KEY, 0, -1)
            ?.contains(userId.toString()) ?: false
    }

    fun getOrder(userId : Long) : Long {
        val queue = redisTemplate.opsForList().range(QUEUE_KEY, 0, -1) ?: return -1
        val index = queue.indexOf(userId.toString())
        return if (index == -1) -1 else index + 1L
    }

    fun getCount(): Long =
        redisTemplate.opsForList().size(QUEUE_KEY) ?: 0L


}