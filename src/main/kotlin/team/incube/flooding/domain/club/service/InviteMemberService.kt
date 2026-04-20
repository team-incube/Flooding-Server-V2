package team.incube.flooding.domain.club.service

interface InviteMemberService {
    fun execute(
        clubId: Long,
        userId: Long,
    )
}
