package team.incube.flooding.domain.ai.song.service.impl

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.ai.song.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.song.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.song.service.RecommendAiSongService
import team.incube.flooding.global.config.AiServerProperties

@Service
class RecommendAiSongServiceImpl(
    restClientBuilder: RestClient.Builder,
    private val aiServerProperties: AiServerProperties,
) : RecommendAiSongService {
    private val restClient = restClientBuilder.build()

    override fun execute(request: RecommendAiSongRequest): RecommendAiSongResponse =
        restClient
            .post()
            .uri(aiServerProperties.url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(RecommendAiSongResponse::class.java) ?: throw RuntimeException("AI 서버로부터 응답을 받지 못했습니다.")
}
