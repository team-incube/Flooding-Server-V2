package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.club.dto.response.ClubApplicationListResponse
import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubApplicationResponse
import team.incube.flooding.domain.club.service.ClubApplicationService
import team.incube.flooding.domain.club.service.CreateClubApplicationService
import team.incube.flooding.domain.club.service.QueryClubApplicationService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리 신청", description = "동아리 개설 및 가입 신청 관리 API")
@RestController
@RequestMapping("/clubs")
class ClubApplicationController(
    private val createClubApplicationService: CreateClubApplicationService,
    private val clubApplicationService: ClubApplicationService,
    private val queryClubApplicationService: QueryClubApplicationService,
) {
    @Operation(summary = "동아리 신청", description = "해당 동아리의 활성 폼에 신청합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "신청 성공"),
        ApiResponse(responseCode = "400", description = "필수 항목 미입력"),
        ApiResponse(responseCode = "404", description = "활성화된 폼 없음"),
        ApiResponse(responseCode = "409", description = "이미 신청한 동아리")
    ])
    @PostMapping("/{clubId}/applications")
    fun createClubApplication(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: CreateClubApplicationRequest,
    ): CommonApiResponse<CreateClubApplicationResponse> =
        CommonApiResponse.success("OK", createClubApplicationService.execute(clubId, request))

    @Operation(summary = "가입 신청 승인", description = "동아리에 가입 신청한 사용자를 승인하여 멤버로 추가합니다")
    @PatchMapping("/{clubId}/applications/{userId}")
    fun approveApplication(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): ResponseEntity<Unit> {
        clubApplicationService.approveApplication(clubId, userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "동아리 개설 신청 목록 조회", description = "Status가 NEW인 동아리 개설 신청 목록을 조회합니다")
    @GetMapping
    fun queryClubApplications(): ResponseEntity<ClubApplicationListResponse> {
        val response = queryClubApplicationService.execute()
        return ResponseEntity.ok(response)
    }
}