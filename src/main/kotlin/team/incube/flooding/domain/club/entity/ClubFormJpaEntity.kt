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
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "tb_club_form")
class ClubFormJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "club_id", nullable = false)
    val club: ClubJpaEntity,
    @field:Column(name = "title", nullable = false)
    val title: String,
    @field:Column(name = "description")
    val description: String?,
    @field:Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    @field:Column(name = "created_at", nullable = false, updatable = false)
    @field:CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @field:Column(name = "updated_at", nullable = false)
    @field:UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
