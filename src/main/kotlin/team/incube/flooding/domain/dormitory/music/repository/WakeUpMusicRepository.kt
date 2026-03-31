package team.incube.flooding.domain.dormitory.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import java.time.LocalDateTime

interface WakeUpMusicRepository : JpaRepository<WakeUpMusicJpaEntity, Long> {
    fun existsByUserId(userId: Long): Boolean

    @Query("""
        SELECT new team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse(
            m.id, m.musicUrl, m.appliedAt, COUNT(l.id))
        FROM WakeUpMusicJpaEntity m LEFT JOIN WakeUpMusicLikeJpaEntity l ON m.id = l.music.id
        WHERE m.appliedAt >= :startOfDay AND m.appliedAt < :endOfDay
        GROUP BY m.id
        ORDER BY m.appliedAt DESC
    """)
    fun findAllWithLikeCountByDate(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<WakeUpMusicResponse>
}