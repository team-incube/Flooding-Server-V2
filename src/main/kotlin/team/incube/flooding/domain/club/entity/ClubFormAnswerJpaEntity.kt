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

@Entity
@Table(
    name = "tb_club_form_answer",
    uniqueConstraints = [UniqueConstraint(columnNames = ["submission_id", "field_id"])],
)
class ClubFormAnswerJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "submission_id", nullable = false)
    val submission: ClubFormSubmissionJpaEntity,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "field_id", nullable = false)
    val field: ClubFormFieldJpaEntity,
    @field:Column(name = "value", columnDefinition = "TEXT")
    val value: String?,
)
