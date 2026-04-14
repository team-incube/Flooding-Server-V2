package team.incube.flooding.domain.club.service

interface ClubMemberService {
    fun inviteMember(
        clubId: Long,
        userId: Long,
    )

    fun transferOwnerShip(
        clubId: Long,
        targetUserId: Long,
    )

    fun exileMember(
        clubId: Long,
        userId: Long,
    )
}
