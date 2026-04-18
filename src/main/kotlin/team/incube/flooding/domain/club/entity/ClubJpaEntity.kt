package team.incube.flooding.domain.club.entity

import jakarta.persistence.*
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.UserJpaEntity

@Entity
@Table(name = "tb_club")
class ClubJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Column(name = "name", nullable = false)
    var name: String,

    @field:Column(name = "type", nullable = false, length = 20)
    @field:Enumerated(EnumType.STRING)
    val type: ClubType,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "leader_id")
    var leader: UserJpaEntity?,

    @field:Column(name = "image_url")
    var imageUrl: String?,

    @field:Column(name = "status", nullable = false, length = 20)
    @field:Enumerated(EnumType.STRING)
    val status: ClubStatus,

    @field:Column(name = "description", length = 1000)
    var description: String?,

    @field:Column(name = "max_member")
    var maxMember: Int?,

    @field:Column(name = "approval_status", nullable = false, length = 20)
    @field:Enumerated(EnumType.STRING)
    var approvalStatus: ClubApprovalStatus = ClubApprovalStatus.PENDING,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "club_room_id")
    var clubRoom: ClubRoomJpaEntity? = null
) {
    fun isModifiableBy(user: UserJpaEntity): Boolean {
        val isAdminOrCouncil = user.role == Role.ADMIN || user.role == Role.STUDENT_COUNCIL
        val isLeader = leader?.id == user.id
        return isAdminOrCouncil || isLeader
    }
}
