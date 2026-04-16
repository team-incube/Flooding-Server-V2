package team.incube.flooding.domain.club.service

interface ClubApplicationService {
    fun execute(
        clubId: Long,
        userId: Long,
    )
}
