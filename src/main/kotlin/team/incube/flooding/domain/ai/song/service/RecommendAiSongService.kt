package team.incube.flooding.domain.ai.song.service

import team.incube.flooding.domain.ai.song.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.song.presentation.data.response.RecommendAiSongResponse

interface RecommendAiSongService {
    fun execute(request: RecommendAiSongRequest): RecommendAiSongResponse
}
