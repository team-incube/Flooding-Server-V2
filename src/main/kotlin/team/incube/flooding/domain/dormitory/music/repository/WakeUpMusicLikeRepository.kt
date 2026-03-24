package team.incube.flooding.domain.dormitory.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicLikeJpaEntity

interface WakeUpMusicLikeRepository : JpaRepository<WakeUpMusicLikeJpaEntity, Long> {
    fun existsByUserIdAndMusicId(userId: Long, musicId: Long): Boolean

    fun findByUserIdAndMusicId(userId: Long, musicId: Long): WakeUpMusicLikeJpaEntity?

    fun countByMusicId(musicId: Long): Long
}