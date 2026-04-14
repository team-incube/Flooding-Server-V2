package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubListResponse
import team.incube.flooding.domain.club.service.CreateAutonomousClubApplicationService
import team.incube.flooding.domain.club.service.DeleteClubService
import team.incube.flooding.domain.club.service.GetClubListService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리", description = "동아리 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubController(
    private val createAutonomousClubApplicationService: CreateAutonomousClubApplicationService,
    private val getClubListService: GetClubListService,
    private val deleteClubService: DeleteClubService,
) {
    @Operation(
        summary = "동아리 삭제",
        description = "동아리를 삭제합니다. ADMIN, STUDENT_COUNCIL은 모든 동아리를 삭제할 수 있으며, 동아리 리더는 본인 동아리만 삭제할 수 있습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 동아리"),
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @PathVariable clubId: Long,
    ): CommonApiResponse<Nothing> {
        deleteClubService.execute(clubId)
        return CommonApiResponse.success("OK")
    }

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
}
