package team.incube.flooding.domain.auth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.auth.adapter.RefreshTokenRedisAdapter
import team.incube.flooding.domain.auth.dto.response.ReissueTokenResponse
import team.incube.flooding.global.auth.provider.JwtProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ReissueTokenService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRedisAdapter: RefreshTokenRedisAdapter,
) {
    fun execute(refreshToken: String): ReissueTokenResponse {
        val userId =
            jwtProvider.getUserIdOrNull(refreshToken)
                ?: throw ExpectedException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)

        val storedToken = refreshTokenRedisAdapter.find(userId)

        if (storedToken != refreshToken) {
            refreshTokenRedisAdapter.delete(userId)
            throw ExpectedException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        val newAccessToken = jwtProvider.generateAccessToken(userId)
        val newRefreshToken = jwtProvider.generateRefreshToken(userId)

        refreshTokenRedisAdapter.save(userId, newRefreshToken)

        return ReissueTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }
}
