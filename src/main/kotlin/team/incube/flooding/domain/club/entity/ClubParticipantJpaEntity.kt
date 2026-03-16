package team.incube.flooding.domain.club.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.io.Serializable

data class ClubParticipantId(
    val club: Long = 0,
    val user: Long = 0,
) : Serializable

@Entity
@IdClass(ClubParticipantId::class)
@Table(name = "tb_club_participant")
class ClubParticipantJpaEntity(
    @field:Id
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "club_id", nullable = false)
    val club: ClubJpaEntity,

    @field:Id
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
)
