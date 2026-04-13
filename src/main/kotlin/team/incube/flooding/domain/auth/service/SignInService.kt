package team.incube.flooding.domain.auth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.auth.dto.response.SignInResponse
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.CreateUserService
import team.incube.flooding.global.auth.provider.JwtProvider
import team.themoment.datagsm.sdk.oauth.DataGsmClient
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class SignInService(
    private val dataGsmClient: DataGsmClient,
    private val userRepository: UserRepository,
    private val createUserService: CreateUserService,
    private val jwtProvider: JwtProvider,
) {
    fun execute(authCode: String): SignInResponse {
        val tokenResponse = dataGsmClient.exchangeToken(authCode)
        val oauthUser = dataGsmClient.getUserInfo(tokenResponse.accessToken)

        if (!oauthUser.isStudent()) throw ExpectedException("학생이 아닙니다.", HttpStatus.FORBIDDEN)

        val student = oauthUser.student!!
        val user = userRepository.findById(student.id).orElse(createUserService.execute(oauthUser))

        return SignInResponse(
            accessToken = jwtProvider.generateAccessToken(user.id),
            refreshToken = jwtProvider.generateRefreshToken(user.id),
        )
    }
}
