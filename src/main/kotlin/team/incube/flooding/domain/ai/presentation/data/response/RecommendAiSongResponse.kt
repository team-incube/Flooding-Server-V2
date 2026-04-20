package team.incube.flooding.domain.ai.presentation.data.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendAiSongResponse(
    @JsonProperty("youtube_links")
    val youtubeLinks: List<String>,
)
