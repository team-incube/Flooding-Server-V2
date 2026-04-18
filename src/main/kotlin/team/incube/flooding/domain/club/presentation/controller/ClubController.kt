package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.presentation.data.request.PutClubRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import team.incube.flooding.domain.club.presentation.data.response.PatchClubApprovalResponse
import team.incube.flooding.domain.club.service.*
import team.themoment.sdk.response.CommonApiResponse
import java.net.URLEncoder

@Tag(name = "동아리", description = "동아리 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubController(
    private val createAutonomousClubApplicationService: CreateAutonomousClubApplicationService,
    private val createClubService: CreateClubService,
    private val patchClubApprovalService: PatchClubApprovalService,
    private val getClubListService: GetClubListService,
    private val deleteClubService: DeleteClubService,
    private val putClubService: PutClubService,
    private val getClubService: GetClubService,
    private val getClubApplicationService: GetClubApplicationService,
    private val downloadClubExcelService: DownloadClubExcelService,
) {
    @Operation(summary = "동아리 개설 신청", description = "새로운 동아리 개설을 신청합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createClub(
        @Valid @RequestBody request: CreateClubRequest,
    ): CommonApiResponse<Nothing> {
        createClubService.execute(request)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "동아리 개설 신청 승인/거부", description = "ADMIN 또는 STUDENT_COUNCIL만 가능합니다.")
    @PatchMapping("/{clubId}/approval")
    fun patchClubApproval(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: PatchClubApprovalRequest,
    ): CommonApiResponse<PatchClubApprovalResponse> =
        CommonApiResponse.success("OK", patchClubApprovalService.execute(clubId, request))

    @Operation(summary = "동아리 삭제", description = "동아리를 삭제합니다.")
    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @PathVariable clubId: Long,
    ): CommonApiResponse<Nothing> {
        deleteClubService.execute(clubId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "동아리 수정", description = "동아리 정보를 수정합니다.")
    @PutMapping("/{clubId}")
    fun putClub(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: PutClubRequest,
    ): CommonApiResponse<Nothing> {
        putClubService.execute(clubId, request)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "자율 동아리 선착순 신청", description = "자율 동아리에 선착순으로 신청합니다.")
    @PostMapping("/{clubId}/autonomous/applications")
    fun createAutonomousClubApplication(
        @PathVariable clubId: Long,
    ): CommonApiResponse<CreateAutonomousClubApplicationResponse> =
        CommonApiResponse.success("OK", createAutonomousClubApplicationService.execute(clubId))

    @Operation(summary = "동아리 목록 조회", description = "타입으로 필터링하고 검색합니다.")
    @GetMapping
    fun getClubList(
        @RequestParam type: ClubType,
        @RequestParam(required = false) name: String?,
    ): CommonApiResponse<GetClubListResponse> = CommonApiResponse.success("OK", getClubListService.execute(type, name))

    @Operation(summary = "동아리 단건 조회", description = "동아리 상세 정보를 조회합니다.")
    @GetMapping("/{clubId}")
    suspend fun getClub(
        @PathVariable clubId: Long,
    ): CommonApiResponse<GetClubResponse> = CommonApiResponse.success("OK", getClubService.execute(clubId))

    @Operation(summary = "동아리 신청서 전체 조회", description = "제출된 모든 신청서와 답변 목록을 조회합니다.")
    @GetMapping("/{clubId}/applications")
    fun getClubApplication(
        @PathVariable clubId: Long,
    ): ResponseEntity<GetClubApplicationResponse> {
        val response = getClubApplicationService.execute(clubId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "전공동아리 전체 명단 엑셀 조회", description = "모든 전공동아리 정보를 엑셀로 내보냅니다.")
    @GetMapping("/export")
    fun exportAllMajorClubExcel(): ResponseEntity<ByteArray> {
        val fileBytes = downloadClubExcelService.execute()
        val fileName = "전공동아리_전체_명단.xlsx"
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$encodedFileName\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(fileBytes)
    }
}
