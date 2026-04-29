package team.incube.flooding.domain.dormitory.music.presentation.data.request

import jakarta.validation.constraints.NotBlank

data class ApplyWakeUpMusicByUrlRequest(
    @field:NotBlank
    val musicUrl: String,
)
