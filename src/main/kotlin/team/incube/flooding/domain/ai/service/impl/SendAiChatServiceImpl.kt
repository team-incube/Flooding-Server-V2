package team.incube.flooding.domain.ai.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.ai.adapter.AiChatbotAdapter
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.incube.flooding.domain.ai.service.SendAiChatService

@Service
class SendAiChatServiceImpl(
    private val aiChatbotAdapter: AiChatbotAdapter,
) : SendAiChatService {
    override fun execute(request: SendAiChatRequest): SendAiChatResponse = aiChatbotAdapter.chat(request)
}
