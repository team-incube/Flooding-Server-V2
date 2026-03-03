package team.incube.flooding.domain.auth.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.auth.dto.request.SignInReqDto
import team.incube.flooding.domain.auth.dto.response.SignInResDto
import team.incube.flooding.domain.auth.service.SignInService

@RestController
@RequestMapping("/v2/auth")
class AuthController(
    private val signInService: SignInService
) {
    @PostMapping("/signin")
    fun signIn(@RequestBody reqDto: SignInReqDto): SignInResDto =
        signInService.execute(reqDto.authCode)
}
