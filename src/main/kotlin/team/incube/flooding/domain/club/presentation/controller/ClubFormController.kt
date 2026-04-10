package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.presentation.data.request.CreateClubFormRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubFormResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubFormResponse
import team.incube.flooding.domain.club.service.CreateClubFormService
import team.incube.flooding.domain.club.service.GetClubFormService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리 폼", description = "동아리 신청 폼 관련 API")
@RestController
@RequestMapping("/club")
class ClubFormController(
    private val createClubFormService: CreateClubFormService,
    private val getClubFormService: GetClubFormService,
) {
    @Operation(summary = "동아리 폼 생성", description = "동아리 신청 폼을 생성합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "폼 생성 성공"),
        ApiResponse(responseCode = "404", description = "동아리 없음"),
    )
    @PostMapping("/{clubId}/forms")
    fun createClubForm(
        @PathVariable clubId: Long,
        @RequestBody request: CreateClubFormRequest,
    ): CommonApiResponse<CreateClubFormResponse> =
        CommonApiResponse.success("OK", createClubFormService.execute(clubId, request))

    @Operation(summary = "동아리 폼 조회", description = "해당 동아리의 활성 신청 폼을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "폼 조회 성공"),
        ApiResponse(responseCode = "404", description = "활성화된 폼 없음"),
    )
    @GetMapping("/{clubId}/forms")
    fun getClubForm(
        @PathVariable clubId: Long,
    ): CommonApiResponse<GetClubFormResponse> = CommonApiResponse.success("OK", getClubFormService.execute(clubId))
}
