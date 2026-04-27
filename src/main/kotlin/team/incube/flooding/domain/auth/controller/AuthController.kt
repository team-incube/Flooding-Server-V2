package team.incube.flooding.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.auth.dto.request.ReissueTokenRequest
import team.incube.flooding.domain.auth.dto.request.SignInRequest
import team.incube.flooding.domain.auth.dto.response.ReissueTokenResponse
import team.incube.flooding.domain.auth.dto.response.SignInResponse
import team.incube.flooding.domain.auth.service.ReissueTokenService
import team.incube.flooding.domain.auth.service.SignInService

@Tag(name = "인증", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val signInService: SignInService,
    private val reissueTokenService: ReissueTokenService,
) {
    @Operation(
        summary = "로그인",
        description = "OAuth 인증 코드를 사용하여 로그인합니다. 학생만 로그인할 수 있으며, 최초 로그인 시 자동으로 회원가입됩니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "403", description = "학생이 아닌 사용자"),
    )
    @PostMapping("/signin")
    fun signIn(
        @RequestBody request: SignInRequest,
    ): SignInResponse = signInService.execute(request.authCode, request.redirectUri)

    @Operation(
        summary = "토큰 재발급",
        description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다. 기존 Refresh Token은 즉시 무효화됩니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰"),
    )
    @PostMapping("/reissue")
    fun reissueToken(
        @Valid @RequestBody request: ReissueTokenRequest,
    ): ReissueTokenResponse = reissueTokenService.execute(request.refreshToken)
}
