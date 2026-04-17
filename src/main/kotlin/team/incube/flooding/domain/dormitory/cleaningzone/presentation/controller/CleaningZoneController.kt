package team.incube.flooding.domain.dormitory.cleaningzone.presentation.controller

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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.AssignCleaningZoneMembersRequest
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.CreateCleaningZoneRequest
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneDetailResponse
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneResponse
import team.incube.flooding.domain.dormitory.cleaningzone.service.AssignCleaningZoneMembersService
import team.incube.flooding.domain.dormitory.cleaningzone.service.CreateCleaningZoneService
import team.incube.flooding.domain.dormitory.cleaningzone.service.GetCleaningZoneService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "기숙사 청소 구역", description = "기숙사 청소 구역 관리 API")
@RestController
@RequestMapping("/dormitory/cleaning-zones")
class CleaningZoneController(
    private val createCleaningZoneService: CreateCleaningZoneService,
    private val getCleaningZoneService: GetCleaningZoneService,
    private val assignCleaningZoneMembersService: AssignCleaningZoneMembersService,
) {
    @Operation(
        summary = "청소 구역 생성",
        description = "새로운 청소 구역을 생성합니다. 관리자만 접근 가능합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "청소 구역 생성 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCleaningZone(
        @Valid @RequestBody request: CreateCleaningZoneRequest,
    ): CommonApiResponse<Long> = CommonApiResponse.success("OK", createCleaningZoneService.execute(request))

    @Operation(
        summary = "청소 구역 목록 조회",
        description = "전체 청소 구역 목록과 각 구역의 배정 인원 수를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "청소 구역 목록 조회 성공"),
    )
    @GetMapping
    fun getCleaningZones(): CommonApiResponse<List<GetCleaningZoneResponse>> =
        CommonApiResponse.success("OK", getCleaningZoneService.executeList())

    @Operation(
        summary = "청소 구역 상세 조회",
        description = "특정 청소 구역의 상세 정보와 배정된 인원을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "청소 구역 상세 조회 성공"),
        ApiResponse(responseCode = "404", description = "청소 구역을 찾을 수 없음"),
    )
    @GetMapping("/{zoneId}")
    fun getCleaningZone(
        @PathVariable zoneId: Long,
    ): CommonApiResponse<GetCleaningZoneDetailResponse> =
        CommonApiResponse.success("OK", getCleaningZoneService.executeOne(zoneId))

    @Operation(
        summary = "청소 구역 인원 배정",
        description = "청소 구역에 인원을 전체 교체 방식으로 배정합니다. 관리자만 접근 가능합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "인원 배정 성공"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "청소 구역을 찾을 수 없음"),
    )
    @PatchMapping("/{zoneId}/members")
    fun assignMembers(
        @PathVariable zoneId: Long,
        @Valid @RequestBody request: AssignCleaningZoneMembersRequest,
    ): CommonApiResponse<Nothing> {
        assignCleaningZoneMembersService.execute(zoneId, request)
        return CommonApiResponse.success("OK")
    }
}
