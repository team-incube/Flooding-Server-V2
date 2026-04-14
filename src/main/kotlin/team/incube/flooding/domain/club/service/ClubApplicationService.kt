package team.incube.flooding.domain.club.service

interface ClubApplicationService {
    fun approveApplication(
        clubId: Long,
        userId: Long,
    )
}
