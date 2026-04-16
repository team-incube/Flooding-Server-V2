package team.incube.flooding.domain.ai.presentation.data.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class RecommendAiSongRequest(
    @JsonProperty("recent_songs")
    @field:Size(max = 5)
    @field:Valid
    val recentSongs: List<SongRequest>,
)
