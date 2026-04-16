package team.incube.flooding.domain.dormitory.penalty.entity

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
@Table(name = "tb_dormitory_penalty_history")
class DormitoryPenaltyHistoryJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "changed_by_id", nullable = false)
    val changedBy: UserJpaEntity,
    @field:Column(name = "previous_score", nullable = false)
    val previousScore: Int,
    @field:Column(name = "new_score", nullable = false)
    val newScore: Int,
    @field:Column(name = "reason", nullable = false)
    val reason: String,
    @field:Column(name = "changed_at", nullable = false)
    val changedAt: LocalDateTime = LocalDateTime.now(),
)
