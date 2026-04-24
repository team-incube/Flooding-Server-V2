package team.incube.flooding.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse
import team.incube.flooding.domain.user.service.GetMeService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "유저", description = "유저 관련 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val getMeService: GetMeService,
) {
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 유저의 정보를 반환합니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping("/me")
    fun getMe(): CommonApiResponse<GetMeResponse> = CommonApiResponse.success("OK", getMeService.execute())
}
