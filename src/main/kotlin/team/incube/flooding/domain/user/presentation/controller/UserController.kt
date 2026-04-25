package team.incube.flooding.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse
import team.incube.flooding.domain.user.presentation.data.response.SearchUsersResponse
import team.incube.flooding.domain.user.service.GetMeService
import team.incube.flooding.domain.user.service.SearchUsersService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "유저", description = "유저 관련 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val getMeService: GetMeService,
    private val searchUsersService: SearchUsersService,
) {
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 유저의 정보를 반환합니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping("/me")
    fun getMe(): CommonApiResponse<GetMeResponse> = CommonApiResponse.success("OK", getMeService.execute())

    @Operation(
        summary = "학생 검색",
        description = "이름 또는 학번으로 학생을 검색합니다. 두 조건 모두 부분 일치하며, 파라미터를 모두 생략하면 전체 학생을 페이지네이션하여 반환합니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping
    fun searchUsers(
        @Parameter(description = "이름 (부분 일치, 대소문자 무시)")
        @RequestParam(required = false) name: String?,
        @Parameter(description = "학번 (앞자리 일치, 예: '1' → 1학년)")
        @RequestParam(required = false) studentNumber: String?,
        @Parameter(description = "페이지 정보 (page, size, sort)")
        @PageableDefault(size = 20, sort = ["studentNumber,asc"])
        pageable: Pageable,
    ): CommonApiResponse<Page<SearchUsersResponse>> =
        CommonApiResponse.success("OK", searchUsersService.execute(name, studentNumber, pageable))
}
