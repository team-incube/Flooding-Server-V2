package team.incube.flooding.domain.dormitory.study.adapter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class StudyRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>
){
    companion object {
        private const val APPLICATION_KEY = "study:application:"
        private const val COUNT_KEY = "study:count"
    }

    private fun ttlUntilMidnight(): Duration {
            val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
            return Duration.between(LocalDateTime.now(), midnight)
    }

    fun saveApplication(userId:Long,status: StudyApplicationStatus) {
        redisTemplate.opsForValue().set(
            "$APPLICATION_KEY:$userId",
            status.name,
            ttlUntilMidnight(),
        )
    }

        fun getApplicationStatus(userId:Long): StudyApplicationStatus? {
            return redisTemplate.opsForValue()
                .get("$APPLICATION_KEY:$userId")
                ?.let { StudyApplicationStatus.valueOf(it) }
        }

    fun decrementCount() {
        redisTemplate.opsForValue().decrement(COUNT_KEY)
        }

    fun incrementCount(): Long  {
            val count = redisTemplate.opsForValue().increment(COUNT_KEY) ?:0
            if(count == 1L) {
                redisTemplate.expire(COUNT_KEY,ttlUntilMidnight())
            }
            return count
        }

    fun getCount(): Long {
        return redisTemplate.opsForValue().get(COUNT_KEY)?.toLong() ?: 0
    }

}