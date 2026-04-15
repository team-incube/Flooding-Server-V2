package team.incube.flooding.domain.ai.presentation.data.request

import jakarta.validation.constraints.NotBlank

data class SongRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val artist: String,
)
