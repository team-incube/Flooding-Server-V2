package team.incube.flooding.domain.dormitory.study.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

@Entity
@Table(name = "tb_study_ban")
class StudyBanJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(nullable = false, name = "user_id")
    val user: UserJpaEntity,
    @field:Column(name = "banned_at", nullable = false)
    val bannedAt: LocalDateTime = LocalDateTime.now(),
    @field:Column(name = "banned_until", nullable = false)
    val bannedUntil: LocalDateTime = LocalDateTime.now().plusWeeks(1),
)
