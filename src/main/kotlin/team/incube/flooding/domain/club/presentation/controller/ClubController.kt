package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import team.incube.flooding.domain.club.presentation.data.response.PatchClubApprovalResponse
import team.incube.flooding.domain.club.service.CreateAutonomousClubApplicationService
import team.incube.flooding.domain.club.service.CreateClubService
import team.incube.flooding.domain.club.service.GetClubListService
import team.incube.flooding.domain.club.service.GetClubService
import team.incube.flooding.domain.club.service.PatchClubApprovalService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리", description = "동아리 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubController(
    private val createAutonomousClubApplicationService: CreateAutonomousClubApplicationService,
    private val patchClubApprovalService: PatchClubApprovalService,
    private val getClubListService: GetClubListService,
    private val getClubService: GetClubService,
    private val createClubService: CreateClubService,
) {
    @Operation(summary = "동아리 개설 신청", description = "새로운 동아리 개설을 신청합니다. 신청 후 status는 NEW로 설정되며 관리자 승인이 필요합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "개설 신청 성공"),
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createClub(
        @Valid @RequestBody request: CreateClubRequest,
    ): CommonApiResponse<Nothing> {
        createClubService.execute(request)
        return CommonApiResponse.success("OK")
    }

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

    @Operation(summary = "동아리 목록 조회", description = "타입으로 필터링하고 동아리명 또는 리더 이름으로 검색합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    fun getClubList(
        @RequestParam type: ClubType,
        @RequestParam(required = false) name: String?,
    ): CommonApiResponse<GetClubListResponse> = CommonApiResponse.success("OK", getClubListService.execute(type, name))

    @Operation(summary = "동아리 단건 조회", description = "동아리 ID로 상세 정보 + 멤버 + 프로젝트를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 동아리"),
    )
    @GetMapping("/{clubId}")
    fun getClub(
        @PathVariable clubId: Long,
    ): CommonApiResponse<GetClubResponse> = CommonApiResponse.success("OK", getClubService.execute(clubId))
}
