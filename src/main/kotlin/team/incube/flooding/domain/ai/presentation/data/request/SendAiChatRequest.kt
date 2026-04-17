package team.incube.flooding.domain.ai.presentation.data.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendAiChatRequest(
    @JsonProperty("user_input")
    @field:NotBlank(message = "메시지를 입력해 주세요.")
    @field:Size(max = 1000, message = "메시지는 1000자 이하로 입력해 주세요.")
    val userInput: String,
)
