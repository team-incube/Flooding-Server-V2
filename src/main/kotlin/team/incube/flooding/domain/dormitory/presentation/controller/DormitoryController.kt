package team.incube.flooding.domain.dormitory.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.presentation.data.response.GetApplicationAvailabilityResponse
import team.incube.flooding.domain.dormitory.service.GetApplicationAvailabilityService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "기숙사", description = "기숙사 공통 API")
@RestController
@RequestMapping("/dormitory")
class DormitoryController(
    private val getApplicationAvailabilityService: GetApplicationAvailabilityService,
) {
    @Operation(
        summary = "신청 가능 여부 조회",
        description = "자습, 홈베이스, 안마의자의 현재 신청 가능 여부를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping("/application-availability")
    fun getApplicationAvailability(): CommonApiResponse<GetApplicationAvailabilityResponse> =
        CommonApiResponse.success("OK", getApplicationAvailabilityService.execute())
}
