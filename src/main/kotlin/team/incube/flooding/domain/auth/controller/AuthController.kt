package team.incube.flooding.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.auth.dto.request.SignInRequest
import team.incube.flooding.domain.auth.dto.response.SignInResponse
import team.incube.flooding.domain.auth.service.SignInService

@Tag(name = "인증", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val signInService: SignInService,
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
    ): SignInResponse = signInService.execute(request.authCode)
}
