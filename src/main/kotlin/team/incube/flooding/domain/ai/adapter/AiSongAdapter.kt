package team.incube.flooding.domain.ai.adapter

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
    restClientBuilder: RestClient.Builder,
) {
    private val restClient =
        restClientBuilder
            .baseUrl(baseUrl)
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(3_000)
                    setReadTimeout(30_000)
                },
            ).build()

    fun recommend(request: RecommendAiSongRequest): RecommendAiSongResponse =
        restClient
            .post()
            .uri("/ai/song")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus({ it.isError }) { _, _ ->
                throw ExpectedException("AI 음악 추천 서버와 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY)
            }.body(RecommendAiSongResponse::class.java)
            ?: throw ExpectedException("AI 음악 추천 서버로부터 응답을 받지 못했습니다.", HttpStatus.BAD_GATEWAY)
}
