package team.incube.flooding.domain.ai.song.presentation.data.request

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendAiSongRequest(
    @JsonProperty("recent_songs")
    val recentSongs: List<SongRequest>,
)
