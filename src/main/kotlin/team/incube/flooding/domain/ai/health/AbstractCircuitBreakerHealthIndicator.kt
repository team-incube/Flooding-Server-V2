package team.incube.flooding.domain.ai.health

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator

abstract class AbstractCircuitBreakerHealthIndicator(
    private val circuitBreaker: CircuitBreaker,
) : HealthIndicator {
    override fun health(): Health {
        val state = circuitBreaker.state
        val metrics = circuitBreaker.metrics

        val details =
            mapOf(
                "state" to state.name,
                "failureRate" to "${metrics.failureRate}%",
                "slowCallRate" to "${metrics.slowCallRate}%",
                "bufferedCalls" to metrics.numberOfBufferedCalls,
                "failedCalls" to metrics.numberOfFailedCalls,
                "notPermittedCalls" to metrics.numberOfNotPermittedCalls,
            )

        return when (state) {
            CircuitBreaker.State.OPEN -> {
                Health.down().withDetails(details).build()
            }

            CircuitBreaker.State.HALF_OPEN -> {
                Health.unknown().withDetails(details).build()
            }

            else -> {
                Health.up().withDetails(details).build()
            }
        }
    }
}
