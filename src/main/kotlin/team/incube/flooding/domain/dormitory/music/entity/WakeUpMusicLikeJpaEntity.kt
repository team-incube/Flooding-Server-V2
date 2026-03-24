package team.incube.flooding.domain.dormitory.music.entity

import jakarta.persistence.*
import team.incube.flooding.domain.user.entity.UserJpaEntity

@Entity
@Table(name = "tb_wake_up_music_like")
class WakeUpMusicLikeJpaEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "music_id", nullable = false)
    val music: WakeUpMusicJpaEntity
)