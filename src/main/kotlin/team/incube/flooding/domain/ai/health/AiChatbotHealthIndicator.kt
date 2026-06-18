package team.incube.flooding.domain.ai.health

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component("aiChatbot")
class AiChatbotHealthIndicator(
    @Qualifier("chatbotCircuitBreaker") circuitBreaker: CircuitBreaker,
) : AbstractCircuitBreakerHealthIndicator(circuitBreaker)
