package team.incube.flooding.domain.club.service

interface TransferOwnershipService {
    fun execute(
        clubId: Long,
        targetUserId: Long,
    )
}
