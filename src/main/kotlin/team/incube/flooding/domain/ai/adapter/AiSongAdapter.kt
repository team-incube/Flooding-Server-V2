package team.incube.flooding.domain.ai.adapter

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.themoment.sdk.exception.ExpectedException

@Component
class AiSongAdapter(
    @Value("\${ai.song.base-url}") baseUrl: String,
    @Qualifier("songCircuitBreaker") private val circuitBreaker: CircuitBreaker,
    @Qualifier("songRetry") private val retry: Retry,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(3_000)
                    setReadTimeout(30_000)
                },
            ).build()

    fun recommend(request: RecommendAiSongRequest): RecommendAiSongResponse {
        val decorated =
            CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                Retry
                    .decorateCheckedSupplier(retry) {
                        doRecommend(request)
                    }.get()
            }

        return try {
            decorated.get()
        } catch (e: CallNotPermittedException) {
            throw ExpectedException("AI 음악 추천 서버가 일시적으로 사용 불가합니다. 잠시 후 다시 시도해주세요.", HttpStatus.SERVICE_UNAVAILABLE)
        } catch (e: ExpectedException) {
            throw e
        } catch (e: Throwable) {
            log.error("AI 음악 추천 서버 통신 실패 - {}", e.message)
            throw ExpectedException("AI 음악 추천 서버와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY)
        }
    }

    private fun doRecommend(request: RecommendAiSongRequest): RecommendAiSongResponse =
        restClient
            .post()
            .uri("/ai/song")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus({ it.isError }) { _, response ->
                val body = response.body.bufferedReader().use { it.readText() }
                log.error("AI 음악 추천 서버 오류 - status: {}, body: {}", response.statusCode, body)
                throw ExpectedException("AI 음악 추천 서버와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY)
            }.body(RecommendAiSongResponse::class.java)
            ?: throw ExpectedException("AI 음악 추천 서버로부터 응답을 받지 못했습니다.", HttpStatus.BAD_GATEWAY)
}
