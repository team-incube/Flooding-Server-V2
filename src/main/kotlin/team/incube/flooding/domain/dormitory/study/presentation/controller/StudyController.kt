package team.incube.flooding.domain.dormitory.study.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyListResponse
import team.incube.flooding.domain.dormitory.study.service.BanStudyService
import team.incube.flooding.domain.dormitory.study.service.CancelStudyService
import team.incube.flooding.domain.dormitory.study.service.CheckStudyAttendanceService
import team.incube.flooding.domain.dormitory.study.service.GetStudyService
import team.incube.flooding.domain.dormitory.study.service.StudyApplicationService
import team.incube.flooding.domain.dormitory.study.service.SubscribeStudyAttendanceService
import team.incube.flooding.domain.dormitory.study.service.UnbanStudyService
import team.incube.flooding.domain.dormitory.study.service.UncheckStudyAttendanceService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "자습", description = "자습 신청 관련 API")
@RestController
@RequestMapping("dormitory/studies")
class StudyController(
    private val studyApplicationService: StudyApplicationService,
    private val cancelStudyService: CancelStudyService,
    private val banStudyService: BanStudyService,
    private val unbanStudyService: UnbanStudyService,
    private val getStudyService: GetStudyService,
    private val subscribeStudyAttendanceService: SubscribeStudyAttendanceService,
    private val checkStudyAttendanceService: CheckStudyAttendanceService,
    private val uncheckStudyAttendanceService: UncheckStudyAttendanceService,
) {
    @Operation(
        summary = "자습 신청자 목록 조회",
        description = "오늘 자습을 신청한 학생 목록을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    fun getStudy(): CommonApiResponse<GetStudyListResponse> = CommonApiResponse.success("OK", getStudyService.execute())

    @Operation(
        summary = "자습 신청",
        description = "자습 신청 시간 내에 자습을 신청합니다. 금지 상태이거나 이미 신청한 경우 신청할 수 없습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "자습 신청 성공"),
        ApiResponse(responseCode = "400", description = "자습 신청 시간이 아님"),
        ApiResponse(responseCode = "403", description = "자습 금지 상태"),
        ApiResponse(responseCode = "409", description = "이미 신청했거나 자습을 취소했거나 인원 마감"),
    )
    @PostMapping
    fun apply(): CommonApiResponse<Nothing> {
        studyApplicationService.execute()
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "자습 취소",
        description = "신청한 자습을 취소합니다. 자습을 신청한 상태에서만 취소할 수 있습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "자습 취소 성공"),
        ApiResponse(responseCode = "404", description = "자습 신청 내역 없음"),
    )
    @DeleteMapping
    fun cancel(): CommonApiResponse<Nothing> {
        cancelStudyService.execute()
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "자습 체크인 SSE 구독",
        description = "자습 체크인 이벤트를 실시간으로 수신합니다. 연결 시 현재까지 체크된 목록을 init 이벤트로 전송합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "SSE 구독 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
    )
    @GetMapping("/attendance", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribeAttendance(): SseEmitter = subscribeStudyAttendanceService.execute()

    @Operation(
        summary = "자습 체크인",
        description = "자습을 신청한 학생의 자습실 도착을 체크합니다. 관리자 또는 기자위 권한이 필요합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "체크인 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 학생 또는 자습 신청 내역 없음"),
        ApiResponse(responseCode = "409", description = "이미 체크인 완료"),
    )
    @PostMapping("/attendance/{userId}")
    fun checkAttendance(
        @Parameter(description = "체크인할 학생의 ID") @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        checkStudyAttendanceService.execute(userId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "자습 체크인 해제",
        description = "자습 체크인된 학생의 체크인을 해제합니다. 관리자 또는 기자위 권한이 필요합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "체크인 해제 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 학생 또는 체크인 기록 없음"),
    )
    @DeleteMapping("/attendance/{userId}")
    fun uncheckAttendance(
        @Parameter(description = "체크인 해제할 학생의 ID") @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        uncheckStudyAttendanceService.execute(userId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "자습 금지",
        description = "특정 학생의 자습을 1주일간 금지합니다. 관리자 또는 기자위 권한이 필요합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "자습 금지 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
        ApiResponse(responseCode = "409", description = "이미 자습 금지 상태"),
    )
    @PostMapping("/ban/{userId}")
    fun ban(
        @Parameter(description = "금지할 학생의 ID") @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        banStudyService.execute(userId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "자습 금지 해제",
        description = "특정 학생의 자습 금지 상태를 해제합니다. 관리자 또는 기자위 권한이 필요합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "자습 금지 해제 성공"),
        ApiResponse(responseCode = "404", description = "존재하지 않는 유저 또는 자습 금지 내역 없음"),
    )
    @DeleteMapping("/ban/{userId}")
    fun unban(
        @Parameter(description = "금지 해제할 학생의 ID") @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        unbanStudyService.execute(userId)
        return CommonApiResponse.success("OK")
    }
}
