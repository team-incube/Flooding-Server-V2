package team.incube.flooding.domain.dormitory.music.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.CancelLikeWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.CancelWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.LikeWakeUpMusicService
import team.themoment.sdk.response.CommonApiResponse
import java.time.LocalDate

@Tag(name = "기상음악", description = "기상음악 신청 관련 API")
@RestController
@RequestMapping("/dormitory/music")
class WakeUpMusicController(
    private val applyWakeUpMusicService: ApplyWakeUpMusicService,
    private val cancelWakeUpMusicService: CancelWakeUpMusicService,
    private val getWakeUpMusicService: GetWakeUpMusicService,
    private val likeWakeUpMusicService: LikeWakeUpMusicService,
    private val cancelLikeWakeUpMusicService: CancelLikeWakeUpMusicService,
) {
    @Operation(
        summary = "기상음악 목록 조회",
        description = "날짜별로 신청된 기상음악 목록을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "기상음악 목록 조회 성공"),
    )
    @GetMapping
    fun getWakeUpMusic(
        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = false)
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
    ): CommonApiResponse<List<WakeUpMusicResponse>> =
        CommonApiResponse.success("OK", getWakeUpMusicService.execute(date ?: LocalDate.now()))

    @Operation(
        summary = "기상음악 신청",
        description = "기상음악을 신청합니다. 이미 신청한 경우 신청할 수 없습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "기상음악 신청 성공"),
        ApiResponse(responseCode = "409", description = "이미 기상음악을 신청함"),
    )
    @PostMapping
    fun applyWakeUpMusic(
        @RequestBody request: ApplyWakeUpMusicRequest,
    ): CommonApiResponse<Nothing> {
        applyWakeUpMusicService.execute(request)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "기상음악 취소",
        description = "신청한 기상음악을 취소합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "기상음악 취소 성공"),
        ApiResponse(responseCode = "404", description = "기상음악 신청 내역 없음"),
    )
    @DeleteMapping("/{musicId}")
    fun cancelWakeUpMusic(
        @PathVariable musicId: Long,
    ): CommonApiResponse<Nothing> {
        cancelWakeUpMusicService.execute(musicId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "기상음악 좋아요",
        description = "기상음악에 좋아요를 누릅니다. 이미 좋아요한 경우 누를 수 없습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "좋아요 성공"),
        ApiResponse(responseCode = "404", description = "기상음악 없음"),
        ApiResponse(responseCode = "409", description = "이미 좋아요한 기상음악"),
    )
    @PostMapping("/{musicId}/like")
    fun likeWakeUpMusic(
        @PathVariable musicId: Long,
    ): CommonApiResponse<Nothing> {
        likeWakeUpMusicService.execute(musicId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "기상음악 좋아요 취소",
        description = "기상음악의 좋아요를 취소합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
        ApiResponse(responseCode = "404", description = "기상음악 또는 좋아요 내역 없음"),
    )
    @DeleteMapping("/{musicId}/like")
    fun cancelLikeWakeUpMusic(
        @PathVariable musicId: Long,
    ): CommonApiResponse<Nothing> {
        cancelLikeWakeUpMusicService.execute(musicId)
        return CommonApiResponse.success("OK")
    }
}
