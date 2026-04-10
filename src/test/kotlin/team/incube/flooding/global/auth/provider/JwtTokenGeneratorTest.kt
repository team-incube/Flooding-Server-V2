package team.incube.flooding.global.auth.provider

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe

class JwtTokenGeneratorTest : BehaviorSpec({
    given("유효한 JWT 설정이 있을 때") {
        val jwtProvider = JwtProvider(
            secret = "Kc2Vq9mR7Xw1eP8nZ6dY4sT3hJ5uA0bL2gF9rN1mQ7xW8eP3nZ6dY4tS3hJ5uA0b",
            accessTokenExpiration = 3600000,
            refreshTokenExpiration = 604800000,
        )

        `when`("액세스 토큰을 생성하면") {
            then("정상 생성된다") {
                val token = jwtProvider.generateAccessToken(userId = 2L)
                token shouldNotBe null
            }
        }
    }
})
