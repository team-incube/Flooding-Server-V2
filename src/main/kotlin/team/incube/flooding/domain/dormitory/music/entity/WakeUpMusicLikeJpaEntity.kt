package team.incube.flooding.domain.dormitory.music.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.flooding.domain.user.entity.UserJpaEntity

@Entity
@Table(name = "tb_wake_up_music_like", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "music_id"])])
class WakeUpMusicLikeJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "music_id", nullable = false)
    val music: WakeUpMusicJpaEntity,
)
