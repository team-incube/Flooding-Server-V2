package team.incube.flooding.domain.dormitory.music.presentation.data.request

import jakarta.validation.constraints.NotBlank

data class ApplyWakeUpMusicRequest(
    @field:NotBlank
    val musicUrl: String,
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val artist: String,
)
