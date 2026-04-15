package team.incube.flooding.domain.ai.service

import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse

interface SendAiChatService {
    fun execute(request: SendAiChatRequest): SendAiChatResponse
}
