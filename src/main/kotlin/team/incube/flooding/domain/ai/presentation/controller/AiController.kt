package team.incube.flooding.domain.ai.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.incube.flooding.domain.ai.service.RecommendAiSongService
import team.incube.flooding.domain.ai.service.SendAiChatService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "AI", description = "AI 관련 API")
@RestController
@RequestMapping("/ai")
class AiController(
    private val sendAiChatService: SendAiChatService,
    private val recommendAiSongService: RecommendAiSongService,
) {
    @Operation(
        summary = "AI 챗봇 메시지 전송",
        description = "사용자 입력을 AI 챗봇 서버로 전달하고 응답을 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "챗봇 응답 성공"),
        ApiResponse(responseCode = "502", description = "AI 챗봇 서버 오류"),
    )
    @PostMapping("/chat")
    fun chat(
        @Valid @RequestBody request: SendAiChatRequest,
    ): CommonApiResponse<SendAiChatResponse> = CommonApiResponse.success("OK", sendAiChatService.execute(request))

    @Operation(
        summary = "AI 음악 추천",
        description = "최근 들은 노래 5곡을 기반으로 AI가 추천하는 유튜브 링크 3개를 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "AI 음악 추천 성공"),
        ApiResponse(responseCode = "502", description = "AI 음악 추천 서버 오류"),
    )
    @PostMapping("/song")
    fun recommendAiSong(
        @Valid @RequestBody request: RecommendAiSongRequest,
    ): CommonApiResponse<RecommendAiSongResponse> =
        CommonApiResponse.success("OK", recommendAiSongService.execute(request))
}
