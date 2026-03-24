package team.incube.flooding.domain.dormitory.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity

interface WakeUpMusicRepository : JpaRepository<WakeUpMusicJpaEntity, Long> {
    fun existsByUserId(userId: Long): Boolean
}