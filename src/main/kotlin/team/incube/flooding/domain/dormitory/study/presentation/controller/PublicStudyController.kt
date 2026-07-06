package team.incube.flooding.domain.dormitory.study.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyAttendanceListResponse
import team.incube.flooding.domain.dormitory.study.service.GetPublicStudyAttendanceListService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "자습(공개)", description = "인증 없이 조회 가능한 자습 관련 공개 API")
@RestController
@RequestMapping("public/study")
class PublicStudyController(
    private val getPublicStudyAttendanceListService: GetPublicStudyAttendanceListService,
) {
    @Operation(
        summary = "최근 1주일 자습 출석자 목록 조회 (공개)",
        description =
            "인증 없이 오늘을 포함한 최근 7일간 날짜별 자습 출석자 이름 리스트를 조회합니다. " +
                "학번, 유저 ID 등 개인 식별 정보는 포함되지 않으며 이름만 제공합니다. " +
                "스트릭(연속 출석) 계산과 같은 부가 로직은 이 API를 사용하는 클라이언트(서드파티)가 직접 수행해야 합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping("/attendances")
    fun getAttendances(): CommonApiResponse<List<GetStudyAttendanceListResponse>> =
        CommonApiResponse.success("OK", getPublicStudyAttendanceListService.execute())
}
