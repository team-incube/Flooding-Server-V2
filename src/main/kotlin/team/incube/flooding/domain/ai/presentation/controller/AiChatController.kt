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
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.incube.flooding.domain.ai.service.SendAiChatService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "AI 챗봇", description = "AI 챗봇 관련 API")
@RestController
@RequestMapping("/ai")
class AiChatController(
    private val sendAiChatService: SendAiChatService,
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
}
