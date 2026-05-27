package team.incube.flooding.domain.dormitory.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicLikeJpaEntity

interface WakeUpMusicLikeRepository : JpaRepository<WakeUpMusicLikeJpaEntity, Long> {
    fun existsByUserIdAndMusicId(
        userId: Long,
        musicId: Long,
    ): Boolean

    fun findByUserIdAndMusicId(
        userId: Long,
        musicId: Long,
    ): WakeUpMusicLikeJpaEntity?

    fun countByMusicId(musicId: Long): Long

    fun deleteAllByMusicId(musicId: Long)

    fun deleteAllByMusicIdIn(musicIds: List<Long>)

    @Query("SELECT l.music.id FROM WakeUpMusicLikeJpaEntity l WHERE l.user.id = :userId AND l.music.id IN :musicIds")
    fun findLikedMusicIdsByUserIdAndMusicIdIn(
        userId: Long,
        musicIds: List<Long>,
    ): List<Long>
}
