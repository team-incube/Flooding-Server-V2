package team.incube.flooding.global.auth.provider

import org.junit.jupiter.api.Test
import team.incube.flooding.global.auth.provider.JwtProvider

class JwtTokenGeneratorTest {
    @Test
    fun generateToken() {
        val jwtProvider =
            JwtProvider(
                secret = "Kc2Vq9mR7Xw1eP8nZ6dY4sT3hJ5uA0bL2gF9rN1mQ7xW8eP3nZ6dY4tS3hJ5uA0b",
                accessTokenExpiration = 3600000,
                refreshTokenExpiration = 604800000,
            )

        val token = jwtProvider.generateAccessToken(userId = 2L)
        println("Bearer $token")
    }
}
