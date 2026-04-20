package team.incube.flooding.domain.dormitory.music.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

@Entity
@Table(name = "tb_wake_up_music")
class WakeUpMusicJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:OneToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserJpaEntity,
    @field:Column(name = "music_url", nullable = false)
    val musicUrl: String,
    @field:Column(name = "applied_at", nullable = false)
    val appliedAt: LocalDateTime = LocalDateTime.now(),
)
