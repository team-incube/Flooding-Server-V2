package team.incube.flooding.domain.dormitory.music.presentation.data.response

import java.time.LocalDateTime

data class WakeUpMusicResponse(
    val id: Long,
    val musicUrl: String,
    val appliedAt: LocalDateTime,
    val likeCount: Long,
    val isLiked: Boolean,
) {
    constructor(id: Long, musicUrl: String, appliedAt: LocalDateTime, likeCount: Long) :
        this(id, musicUrl, appliedAt, likeCount, false)
}
