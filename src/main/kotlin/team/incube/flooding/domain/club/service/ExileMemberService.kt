package team.incube.flooding.domain.club.service

interface ExileMemberService {
    fun execute(
        clubId: Long,
        userId: Long,
    )
}
