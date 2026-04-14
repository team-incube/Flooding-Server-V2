package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.PatchClubApprovalResponse
import team.incube.flooding.domain.club.service.CreateAutonomousClubApplicationService
import team.incube.flooding.domain.club.service.PatchClubApprovalService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리", description = "동아리 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubController(
    private val createAutonomousClubApplicationService: CreateAutonomousClubApplicationService,
    private val patchClubApprovalService: PatchClubApprovalService,
) {
    @Operation(summary = "동아리 개설 신청 승인/거부", description = "동아리 개설 신청을 승인하거나 거부합니다. ADMIN 또는 STUDENT_COUNCIL만 호출 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "처리 성공"),
        ApiResponse(responseCode = "400", description = "승인 대기 중인 동아리가 아님"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 동아리"),
    )
    @PatchMapping("/{clubId}/approval")
    fun patchClubApproval(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: PatchClubApprovalRequest,
    ): CommonApiResponse<PatchClubApprovalResponse> =
        CommonApiResponse.success("OK", patchClubApprovalService.execute(clubId, request))

    @Operation(summary = "자율 동아리 선착순 신청", description = "자율 동아리에 선착순으로 신청합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "신청 성공"),
        ApiResponse(responseCode = "400", description = "자율 동아리가 아니거나 정원 미설정"),
        ApiResponse(responseCode = "409", description = "이미 신청하였거나 정원 마감"),
        ApiResponse(responseCode = "429", description = "동시 요청 초과"),
    )
    @PostMapping("/{clubId}/autonomous/applications")
    fun createAutonomousClubApplication(
        @PathVariable clubId: Long,
    ): CommonApiResponse<CreateAutonomousClubApplicationResponse> =
        CommonApiResponse.success("OK", createAutonomousClubApplicationService.execute(clubId))
}
