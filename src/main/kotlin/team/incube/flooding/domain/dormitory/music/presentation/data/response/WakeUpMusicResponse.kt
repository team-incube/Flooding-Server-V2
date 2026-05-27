package team.incube.flooding.domain.dormitory.music.presentation.data.response

import java.time.LocalDateTime

data class WakeUpMusicResponse(
    val id: Long,
    val userId: Long,
    val musicUrl: String,
    val title: String?,
    val artist: String?,
    val duration: String?,
    val durationText: String?,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val appliedAt: LocalDateTime,
    val likeCount: Long,
    val isLiked: Boolean = false,
)
