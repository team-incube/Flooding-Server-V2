package team.incube.flooding.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.incube.flooding.domain.user.entity.UserJpaEntity

@Entity
@Table(name = "tb_club")
class ClubJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:Column(name = "name", nullable = false)
    val name: String,
    @field:Column(name = "type", nullable = false, length = 20)
    @field:Enumerated(EnumType.STRING)
    val type: ClubType,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "leader_id")
    var leader: UserJpaEntity?,
    @field:Column(name = "image_url")
    val imageUrl: String?,
    @field:Column(name = "status", nullable = false, length = 20)
    @field:Enumerated(EnumType.STRING)
    val status: ClubStatus,
    @field:Column(name = "description", length = 1000)
    val description: String?,
    @field:Column(name = "max_member")
    val maxMember: Int?
)
