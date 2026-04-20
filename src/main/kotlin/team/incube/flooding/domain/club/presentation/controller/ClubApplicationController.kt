package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationListResponse
import team.incube.flooding.domain.club.service.CreateClubApplicationService
import team.incube.flooding.domain.club.service.GetClubApplicationListService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리 신청", description = "동아리 신청 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubApplicationController(
    private val createClubApplicationService: CreateClubApplicationService,
    private val getClubApplicationListService: GetClubApplicationListService,
) {
    @Operation(summary = "동아리 신청", description = "해당 동아리의 활성 폼에 신청합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "신청 성공"),
        ApiResponse(responseCode = "400", description = "필수 항목 미입력"),
        ApiResponse(responseCode = "404", description = "활성화된 폼 없음"),
        ApiResponse(responseCode = "409", description = "이미 신청한 동아리"),
    )
    @PostMapping("/{clubId}/applications")
    fun createClubApplication(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: CreateClubApplicationRequest,
    ): CommonApiResponse<CreateClubApplicationResponse> =
        CommonApiResponse.success("OK", createClubApplicationService.execute(clubId, request))

    @Operation(summary = "동아리 신청 목록 조회", description = "정규 동아리의 폼 신청 목록을 조회합니다. 리더 또는 ADMIN/STUDENT_COUNCIL만 호출 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "400", description = "정규 동아리가 아님"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 동아리 또는 생성된 폼 없음"),
    )
    @GetMapping("/{clubId}/applications")
    fun getClubApplicationList(
        @PathVariable clubId: Long,
    ): CommonApiResponse<GetClubApplicationListResponse> =
        CommonApiResponse.success("OK", getClubApplicationListService.execute(clubId))
}
