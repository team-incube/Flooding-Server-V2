package team.incube.flooding.domain.ai.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.ai.adapter.AiChatbotAdapter
import team.incube.flooding.domain.ai.presentation.data.request.SendAiChatRequest
import team.incube.flooding.domain.ai.presentation.data.response.SendAiChatResponse
import team.incube.flooding.domain.ai.service.impl.SendAiChatServiceImpl
import team.themoment.sdk.exception.ExpectedException

class SendAiChatServiceTest :
    BehaviorSpec({
        val adapter = mockk<AiChatbotAdapter>()
        val service = SendAiChatServiceImpl(adapter)

        Given("챗봇 어댑터가 정상 응답을 반환할 때") {
            every { adapter.chat(any()) } returns SendAiChatResponse("안녕하세요!")

            When("서비스를 실행하면") {
                val result = service.execute(SendAiChatRequest("안녕"))

                Then("챗봇 응답이 반환된다") {
                    result.response shouldBe "안녕하세요!"
                }
            }
        }

        Given("챗봇 어댑터에 요청이 전달될 때") {
            val requestSlot = slot<SendAiChatRequest>()
            every { adapter.chat(capture(requestSlot)) } returns SendAiChatResponse("응답")

            When("서비스를 실행하면") {
                service.execute(SendAiChatRequest("테스트 메시지"))

                Then("요청이 어댑터에 그대로 전달된다") {
                    requestSlot.captured.userInput shouldBe "테스트 메시지"
                }
            }
        }

        Given("챗봇 서버가 오류를 반환할 때") {
            every { adapter.chat(any()) } throws
                ExpectedException("AI 챗봇 서버와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY)

            When("서비스를 실행하면") {
                Then("BAD_GATEWAY 예외가 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(SendAiChatRequest("안녕"))
                        }
                    exception.statusCode shouldBe HttpStatus.BAD_GATEWAY
                }
            }
        }
    })
