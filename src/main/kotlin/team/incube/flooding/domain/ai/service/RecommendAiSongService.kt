package team.incube.flooding.domain.ai.service

import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse

interface RecommendAiSongService {
    fun execute(request: RecommendAiSongRequest): RecommendAiSongResponse
}
