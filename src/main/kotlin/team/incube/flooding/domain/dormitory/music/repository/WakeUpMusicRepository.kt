package team.incube.flooding.domain.dormitory.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import java.time.LocalDateTime

interface WakeUpMusicRepository : JpaRepository<WakeUpMusicJpaEntity, Long> {
    fun findTop5ByUserIdOrderByAppliedAtDesc(userId: Long): List<WakeUpMusicJpaEntity>

    fun findAllByUserIdOrderByAppliedAtAsc(userId: Long): List<WakeUpMusicJpaEntity>

    fun existsByUserIdAndAppliedAtBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): Boolean

    @Query(
        """
        SELECT new team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse(
            m.id, m.user.id, m.user.name, m.user.studentNumber, m.musicUrl, m.title, m.artist, m.duration, m.durationText, m.thumbnailUrl, m.videoUrl, m.appliedAt, COUNT(l.id), false)
        FROM WakeUpMusicJpaEntity m LEFT JOIN WakeUpMusicLikeJpaEntity l ON l.music = m
        WHERE m.appliedAt >= :startOfDay AND m.appliedAt < :endOfDay
        GROUP BY m.id, m.user.id, m.user.name, m.user.studentNumber, m.musicUrl, m.title, m.artist, m.duration, m.durationText, m.thumbnailUrl, m.videoUrl, m.appliedAt
    """,
    )
    fun findAllWithLikeCountByDate(
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime,
    ): List<WakeUpMusicResponse>
}
