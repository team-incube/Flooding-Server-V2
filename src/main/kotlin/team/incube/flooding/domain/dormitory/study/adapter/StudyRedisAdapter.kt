package team.incube.flooding.domain.dormitory.study.adapter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class StudyRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val clock: Clock,
) {
    companion object {
        private const val APPLICATION_KEY = "study:application"
        private const val COUNT_KEY = "study:count"
        private const val APPLICANTS_KEY = "study:applicants"
        private const val ATTENDANCE_KEY = "study:attendance"
    }

    private fun ttlUntil6AM(): Duration {
        val now = LocalDateTime.now(clock)
        val next6AM =
            if (now.hour < 6) {
                LocalDateTime.of(now.toLocalDate(), LocalTime.of(6, 0))
            } else {
                LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(6, 0))
            }
        return Duration.between(now, next6AM)
    }

    fun saveApplication(
        userId: Long,
        status: StudyApplicationStatus,
    ) {
        redisTemplate.opsForValue().set(
            "$APPLICATION_KEY:$userId",
            status.name,
            ttlUntil6AM(),
        )
    }

    fun getApplicationStatus(userId: Long): StudyApplicationStatus? =
        redisTemplate.opsForValue().get("$APPLICATION_KEY:$userId")?.let { StudyApplicationStatus.valueOf(it) }

    fun decrementCount() {
        redisTemplate.opsForValue().decrement(COUNT_KEY)
    }

    fun incrementCount(): Long {
        val count = redisTemplate.opsForValue().increment(COUNT_KEY) ?: 0
        redisTemplate.expire(COUNT_KEY, ttlUntil6AM())
        return count
    }

    fun getCount(): Long = redisTemplate.opsForValue().get(COUNT_KEY)?.toLong() ?: 0

    fun addApplicant(userId: Long) {
        val size = redisTemplate.opsForList().rightPush(APPLICANTS_KEY, userId.toString())
        if (size == 1L) {
            redisTemplate.expire(APPLICANTS_KEY, ttlUntil6AM())
        }
    }

    fun removeApplicant(userId: Long) {
        redisTemplate.opsForList().remove(APPLICANTS_KEY, 1, userId.toString())
    }

    fun getApplicantIds(): List<Long> =
        redisTemplate
            .opsForList()
            .range(APPLICANTS_KEY, 0, -1)
            ?.map { it.toLong() } ?: emptyList()

    fun checkAttendance(userId: Long) {
        redisTemplate.opsForSet().add(ATTENDANCE_KEY, userId.toString())
        redisTemplate.expire(ATTENDANCE_KEY, ttlUntil6AM())
    }

    fun isAttendanceChecked(userId: Long): Boolean =
        redisTemplate.opsForSet().isMember(ATTENDANCE_KEY, userId.toString()) ?: false

    fun cancelAttendance(userId: Long) {
        redisTemplate.opsForSet().remove(ATTENDANCE_KEY, userId.toString())
    }

    fun getAttendanceIds(): Set<Long> =
        redisTemplate
            .opsForSet()
            .members(ATTENDANCE_KEY)
            ?.map { it.toLong() }
            ?.toSet() ?: emptySet()

    fun resetAll() {
        val scanOptions =
            ScanOptions
                .scanOptions()
                .match("$APPLICATION_KEY:*")
                .count(100)
                .build()
        val applicationKeys = mutableListOf<String>()
        redisTemplate.execute { connection ->
            connection.scan(scanOptions).use { cursor ->
                cursor.forEach { applicationKeys.add(String(it)) }
            }
            null
        }
        if (applicationKeys.isNotEmpty()) redisTemplate.delete(applicationKeys)
        redisTemplate.delete(COUNT_KEY)
        redisTemplate.delete(APPLICANTS_KEY)
        redisTemplate.delete(ATTENDANCE_KEY)
    }
}
