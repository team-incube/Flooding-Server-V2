package team.incube.flooding.domain.ai.health

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component("aiSong")
class AiSongHealthIndicator(
    @Qualifier("songCircuitBreaker") circuitBreaker: CircuitBreaker,
) : AbstractCircuitBreakerHealthIndicator(circuitBreaker)
