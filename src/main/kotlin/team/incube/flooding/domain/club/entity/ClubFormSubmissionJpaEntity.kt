package team.incube.flooding.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

@Entity
@Table(
    name = "tb_club_form_submission",
    uniqueConstraints = [UniqueConstraint(columnNames = ["form_id", "user_id"])],
)
class ClubFormSubmissionJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "form_id", nullable = false)
    val form: ClubFormJpaEntity,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    @field:Column(name = "submitted_at", nullable = false, updatable = false)
    @field:CreationTimestamp
    val submittedAt: LocalDateTime = LocalDateTime.now(),
)
