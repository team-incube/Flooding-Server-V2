package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse
import team.incube.flooding.domain.club.service.CreateAutonomousClubApplicationService
import team.incube.flooding.domain.club.service.DownloadClubExcelService
import team.incube.flooding.domain.club.service.GetClubApplicationService
import team.incube.flooding.domain.club.service.GetClubListService
import team.themoment.sdk.response.CommonApiResponse
import java.net.URLEncoder

@Tag(name = "동아리", description = "동아리 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubController(
    private val createAutonomousClubApplicationService: CreateAutonomousClubApplicationService,
    private val getClubListService: GetClubListService,
    private val getClubApplicationService: GetClubApplicationService,
    private val downloadClubExcelService: DownloadClubExcelService,
) {
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

    @Operation(summary = "동아리 신청서 전체 조회", description = "특정 동아리에 제출된 모든 신청서와 답변 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "학생 권한이 아님"),
        ApiResponse(responseCode = "404", description = "해당 동아리나 신청서 폼을 찾을 수 없음"),
    )
    @GetMapping("/{clubId}/applications")
    fun getClubApplication(
        @PathVariable clubId: Long,
    ): ResponseEntity<GetClubApplicationResponse> {
        val response = getClubApplicationService.execute(clubId)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "전공동아리 전체 명단 엑셀 조회",
        description = "모든 전공동아리의 학년별 명단 및 활동실 정보를 엑셀로 내보냅니다.",
    )
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
