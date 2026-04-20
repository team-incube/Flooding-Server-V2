package team.incube.flooding.domain.club.entity

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
@Table(
    name = "tb_club_autonomous_application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["club_id", "user_id"])],
)
class ClubAutonomousApplicationJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "club_id", nullable = false)
    val club: ClubJpaEntity,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
)
