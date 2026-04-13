package team.incube.flooding.domain.dormitory.penalty.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.penalty.presentation.data.request.SetPenaltyRequest
import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetMyPenaltyResponse
import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetPenaltyResponse
import team.incube.flooding.domain.dormitory.penalty.service.GetAllPenaltyService
import team.incube.flooding.domain.dormitory.penalty.service.GetMyPenaltyService
import team.incube.flooding.domain.dormitory.penalty.service.SetPenaltyService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "기숙사 벌점", description = "기숙사 벌점 관리 API")
@RestController
@RequestMapping("/dormitory/penalties")
class DormitoryPenaltyController(
    private val getMyPenaltyService: GetMyPenaltyService,
    private val getAllPenaltyService: GetAllPenaltyService,
    private val setPenaltyService: SetPenaltyService,
) {
    @Operation(
        summary = "내 벌점 조회",
        description = "현재 로그인한 유저의 벌점을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "내 벌점 조회 성공"),
    )
    @GetMapping("/me")
    fun getMyPenalty(): CommonApiResponse<GetMyPenaltyResponse> =
        CommonApiResponse.success("OK", getMyPenaltyService.execute())

    @Operation(
        summary = "전체 벌점 조회",
        description = "전체 유저의 벌점을 벌점 높은 순, 학번 낮은 순으로 페이지네이션하여 조회합니다. 관리자만 접근 가능합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "전체 벌점 조회 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
    )
    @GetMapping
    fun getAllPenalties(
        @Parameter(description = "페이지 정보 (page, size)")
        @PageableDefault(size = 20, sort = ["penaltyScore"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): CommonApiResponse<Page<GetPenaltyResponse>> =
        CommonApiResponse.success("OK", getAllPenaltyService.execute(pageable))

    @Operation(
        summary = "벌점 설정",
        description = "특정 유저의 벌점을 절대값으로 설정합니다. 관리자만 접근 가능합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "벌점 설정 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
    )
    @PutMapping("/{userId}")
    fun setPenalty(
        @PathVariable userId: Long,
        @Valid @RequestBody request: SetPenaltyRequest,
    ): CommonApiResponse<Nothing> {
        setPenaltyService.execute(userId, request)
        return CommonApiResponse.success("OK")
    }
}
