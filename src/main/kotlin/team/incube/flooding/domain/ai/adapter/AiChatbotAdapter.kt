package team.incube.flooding.domain.ai.adapter

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.themoment.sdk.exception.ExpectedException

@Component
class AiChatbotAdapter(
    @Value("\${ai.chatbot.base-url}") baseUrl: String,
) {
    private val restClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(3_000)
                    setReadTimeout(10_000)
                },
            ).build()

    fun chat(request: SendAiChatRequest): SendAiChatResponse =
        restClient
            .post()
            .uri("/ai/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExpectedException("AI 챗봇 서버와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY)
            }.body(SendAiChatResponse::class.java)
            ?: throw ExpectedException("AI 챗봇 서버로부터 응답을 받지 못했습니다.", HttpStatus.BAD_GATEWAY)
}
