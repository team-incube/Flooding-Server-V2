package team.incube.flooding.domain.ai.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.ai.adapter.AiChatbotAdapter
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.incube.flooding.domain.ai.service.impl.SendAiChatServiceImpl
import team.themoment.sdk.exception.ExpectedException
import kotlin.test.assertFailsWith

class SendAiChatServiceTest {
    private fun stubAdapter(response: SendAiChatResponse): AiChatbotAdapter =
        object : AiChatbotAdapter("http://localhost") {
            override fun chat(request: SendAiChatRequest): SendAiChatResponse = response
        }

    private fun errorAdapter(): AiChatbotAdapter =
        object : AiChatbotAdapter("http://localhost") {
            override fun chat(request: SendAiChatRequest): SendAiChatResponse =
                throw ExpectedException("AI 챗봇 서버로부터 응답을 받지 못했습니다.", HttpStatus.BAD_GATEWAY)
        }

    @Test
    fun `챗봇 응답이 정상적으로 반환된다`() {
        val expected = SendAiChatResponse("안녕하세요!")
        val service = SendAiChatServiceImpl(stubAdapter(expected))

        val result = service.execute(SendAiChatRequest("안녕"))

        assertEquals("안녕하세요!", result.response)
    }

    @Test
    fun `요청이 어댑터에 그대로 전달된다`() {
        var captured: SendAiChatRequest? = null
        val adapter =
            object : AiChatbotAdapter("http://localhost") {
                override fun chat(request: SendAiChatRequest): SendAiChatResponse {
                    captured = request
                    return SendAiChatResponse("응답")
                }
            }
        val service = SendAiChatServiceImpl(adapter)

        service.execute(SendAiChatRequest("테스트 메시지"))

        assertEquals("테스트 메시지", captured?.userInput)
    }

    @Test
    fun `챗봇 서버 오류 시 BAD_GATEWAY 예외가 발생한다`() {
        val service = SendAiChatServiceImpl(errorAdapter())

        val exception =
            assertFailsWith<ExpectedException> {
                service.execute(SendAiChatRequest("안녕"))
            }

        assertEquals(HttpStatus.BAD_GATEWAY, exception.statusCode)
    }
}
