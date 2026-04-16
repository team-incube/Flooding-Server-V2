package team.incube.flooding.domain.ai.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.ai.adapter.AiSongAdapter
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.service.RecommendAiSongService

@Service
class RecommendAiSongServiceImpl(
    private val aiSongAdapter: AiSongAdapter,
) : RecommendAiSongService {
    override fun execute(request: RecommendAiSongRequest): RecommendAiSongResponse = aiSongAdapter.recommend(request)
}
