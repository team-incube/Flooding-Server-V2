package team.incube.flooding.domain.auth.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.auth.adapter.RefreshTokenRedisAdapter
import team.incube.flooding.domain.auth.dto.response.SignInResponse
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.CreateUserService
import team.incube.flooding.global.auth.provider.JwtProvider
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient
import team.themoment.datagsm.sdk.oauth.exception.DataGsmException
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class SignInService(
    private val dataGsmClient: DataGsmOAuthClient,
    private val userRepository: UserRepository,
    private val createUserService: CreateUserService,
    private val jwtProvider: JwtProvider,
    private val refreshTokenRedisAdapter: RefreshTokenRedisAdapter,
) {
    private val logger = LoggerFactory.getLogger(SignInService::class.java)

    fun execute(
        authCode: String,
        redirectUri: String,
    ): SignInResponse {
        val oauthUser =
            try {
                val tokenResponse = dataGsmClient.exchangeCodeForToken(authCode, redirectUri)
                dataGsmClient.getUserInfo(tokenResponse.accessToken)
            } catch (ex: DataGsmException) {
                logger.warn("DataGSM OAuth SDK error (status={}): {}", ex.statusCode, ex.message)
                val (status, message) = mapOAuthError(ex.statusCode)
                throw ExpectedException(message, status)
            }

        if (!oauthUser.isStudent()) throw ExpectedException("학생이 아닙니다.", HttpStatus.FORBIDDEN)

        val student =
            oauthUser.student ?: throw ExpectedException("학생 정보를 가져올 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        val user = userRepository.findById(student.id).orElse(createUserService.execute(oauthUser))

        val accessToken = jwtProvider.generateAccessToken(user.id)
        val refreshToken = jwtProvider.generateRefreshToken(user.id)

        refreshTokenRedisAdapter.save(user.id, refreshToken)

        return SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun mapOAuthError(statusCode: Int): Pair<HttpStatus, String> =
        when (statusCode) {
            400 -> HttpStatus.BAD_REQUEST to "유효하지 않거나 만료된 인증 코드입니다."
            401 -> HttpStatus.UNAUTHORIZED to "OAuth 인증에 실패했습니다."
            403 -> HttpStatus.FORBIDDEN to "OAuth 권한이 부족합니다."
            404 -> HttpStatus.NOT_FOUND to "OAuth 리소스를 찾을 수 없습니다."
            429 -> HttpStatus.TOO_MANY_REQUESTS to "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
            500 -> HttpStatus.INTERNAL_SERVER_ERROR to "OAuth 서버 오류가 발생했습니다."
            else -> HttpStatus.INTERNAL_SERVER_ERROR to "OAuth 인증 처리에 실패했습니다."
        }
}
