package team.incube.flooding.domain.ai.song.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.ai.song.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.song.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.song.service.RecommendAiSongService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "AI 음악 추천", description = "AI 기반 음악 추천 API")
@RestController
@RequestMapping("/ai")
class AiSongController(
    private val recommendAiSongService: RecommendAiSongService,
) {
    @Operation(
        summary = "AI 음악 추천",
        description = "최근 들은 노래 5곡을 기반으로 AI가 추천하는 유튜브 링크 3개를 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "AI 음악 추천 성공"),
    )
    @PostMapping("/song")
    fun recommendAiSong(
        @RequestBody @Valid request: RecommendAiSongRequest,
    ): CommonApiResponse<RecommendAiSongResponse> =
        CommonApiResponse.success("OK", recommendAiSongService.execute(request))
}
