package team.incube.flooding.domain.auth.adapter

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpirationMs: Long,
) {
    companion object {
        private const val KEY_PREFIX = "refresh:token:"
    }

    fun save(
        userId: Long,
        refreshToken: String,
    ) {
        redisTemplate.opsForValue().set(
            "$KEY_PREFIX$userId",
            refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs),
        )
    }

    fun find(userId: Long): String? = redisTemplate.opsForValue().get("$KEY_PREFIX$userId")

    fun delete(userId: Long) {
        redisTemplate.delete("$KEY_PREFIX$userId")
    }
}
