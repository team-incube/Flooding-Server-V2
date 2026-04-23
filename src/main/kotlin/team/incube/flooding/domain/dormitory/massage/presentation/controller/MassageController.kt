package team.incube.flooding.domain.dormitory.massage.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse
import team.incube.flooding.domain.dormitory.massage.service.ApplyMassageService
import team.incube.flooding.domain.dormitory.massage.service.CancelMassageService
import team.incube.flooding.domain.dormitory.massage.service.GetMassageService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "마사지", description = "마사지 신청 관련 API")
@RestController
@RequestMapping("/dormitory/massages")
class MassageController(
    private val applyMassageService: ApplyMassageService,
    private val cancelMassageService: CancelMassageService,
    private val getMassageService: GetMassageService,
) {
    @Operation(
        summary = "안마의자 신청 현황 조회",
        description = "현재 사용자의 안마의자 신청 여부, 대기 순번, 전체 신청 인원을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    fun getMassage(): CommonApiResponse<GetMassageResponse> = CommonApiResponse.success(getMassageService.execute())

    @Operation(
        summary = "마사지 신청",
        description = "마사지 신청 시간 내에 마사지를 신청합니다. 이미 신청한 경우 신청할 수 없습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "마사지 신청 성공"),
        ApiResponse(responseCode = "400", description = "마사지 신청 시간이 아님"),
        ApiResponse(responseCode = "409", description = "이미 신청했거나 인원 마감"),
    )
    @PostMapping
    fun applyMassage(): CommonApiResponse<Nothing> {
        applyMassageService.execute()
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "마사지 취소",
        description = "신청한 마사지를 취소합니다. 마사지를 신청한 상태에서만 취소할 수 있습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "마사지 취소 성공"),
        ApiResponse(responseCode = "404", description = "마사지 신청 내역 없음"),
    )
    @DeleteMapping
    fun cancelMassage(): CommonApiResponse<Nothing> {
        cancelMassageService.execute()
        return CommonApiResponse.success("OK")
    }
}
