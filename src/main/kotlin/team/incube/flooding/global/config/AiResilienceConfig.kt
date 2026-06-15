package team.incube.flooding.global.config

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.sdk.exception.ExpectedException
import java.time.Duration

@Configuration
class AiResilienceConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean("chatbotCircuitBreaker")
    fun chatbotCircuitBreaker(): CircuitBreaker {
        val config =
            CircuitBreakerConfig
                .custom()
                .failureRateThreshold(50f)
                .slowCallRateThreshold(80f)
                .slowCallDurationThreshold(Duration.ofSeconds(8))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .ignoreExceptions(ExpectedException::class.java)
                .build()

        val circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("ai-chatbot")

        circuitBreaker.eventPublisher.onStateTransition { event ->
            log.warn(
                "AI 챗봇 Circuit Breaker 상태 변경: {} → {}",
                event.stateTransition.fromState,
                event.stateTransition.toState,
            )
        }
        circuitBreaker.eventPublisher.onCallNotPermitted {
            log.warn("AI 챗봇 Circuit Breaker OPEN - 요청 차단됨")
        }

        return circuitBreaker
    }

    @Bean("songCircuitBreaker")
    fun songCircuitBreaker(): CircuitBreaker {
        val config =
            CircuitBreakerConfig
                .custom()
                .failureRateThreshold(50f)
                .slowCallRateThreshold(80f)
                .slowCallDurationThreshold(Duration.ofSeconds(25))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .ignoreExceptions(ExpectedException::class.java)
                .build()

        val circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("ai-song")

        circuitBreaker.eventPublisher.onStateTransition { event ->
            log.warn(
                "AI 음악 추천 Circuit Breaker 상태 변경: {} → {}",
                event.stateTransition.fromState,
                event.stateTransition.toState,
            )
        }
        circuitBreaker.eventPublisher.onCallNotPermitted {
            log.warn("AI 음악 추천 Circuit Breaker OPEN - 요청 차단됨")
        }

        return circuitBreaker
    }

    @Bean("chatbotRetry")
    fun chatbotRetry(): Retry {
        val config =
            RetryConfig
                .custom<Any>()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(ResourceAccessException::class.java)
                .ignoreExceptions(ExpectedException::class.java)
                .build()

        val retry = RetryRegistry.of(config).retry("ai-chatbot")

        retry.eventPublisher.onRetry { event ->
            log.warn(
                "AI 챗봇 재시도 중 - 시도 횟수: {}, 원인: {}",
                event.numberOfRetryAttempts,
                event.lastThrowable?.message,
            )
        }
        retry.eventPublisher.onError { event ->
            log.error(
                "AI 챗봇 최대 재시도 초과 - 총 시도: {}, 원인: {}",
                event.numberOfRetryAttempts,
                event.lastThrowable?.message,
            )
        }

        return retry
    }

    @Bean("songRetry")
    fun songRetry(): Retry {
        val config =
            RetryConfig
                .custom<Any>()
                .maxAttempts(2)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(ResourceAccessException::class.java)
                .ignoreExceptions(ExpectedException::class.java)
                .build()

        val retry = RetryRegistry.of(config).retry("ai-song")

        retry.eventPublisher.onRetry { event ->
            log.warn(
                "AI 음악 추천 재시도 중 - 시도 횟수: {}, 원인: {}",
                event.numberOfRetryAttempts,
                event.lastThrowable?.message,
            )
        }
        retry.eventPublisher.onError { event ->
            log.error(
                "AI 음악 추천 최대 재시도 초과 - 총 시도: {}, 원인: {}",
                event.numberOfRetryAttempts,
                event.lastThrowable?.message,
            )
        }

        return retry
    }
}
