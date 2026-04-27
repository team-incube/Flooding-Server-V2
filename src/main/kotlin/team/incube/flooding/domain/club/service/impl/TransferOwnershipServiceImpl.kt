package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.TransferOwnershipService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class TransferOwnershipServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : TransferOwnershipService {
    @Transactional
    override fun execute(
        clubId: Long,
        targetUserId: Long,
    ) {
        val currentUser = currentUserProvider.getCurrentUser()
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        if (club.leader?.id != currentUser.id) {
            throw ExpectedException("권한이 없습니다", HttpStatus.FORBIDDEN)
        }

        if (!clubParticipantJpaRepository.existsById(ClubParticipantId(clubId, targetUserId))) {
            throw ExpectedException("동아리 멤버가 아닙니다", HttpStatus.BAD_REQUEST)
        }

        val targetUser =
            userRepository
                .findById(targetUserId)
                .orElseThrow { ExpectedException("대상 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        if (club.leader?.id == targetUserId) {
            throw ExpectedException("이미 해당 유저가 동아리 방장입니다", HttpStatus.BAD_REQUEST)
        }

        club.leader = targetUser
    }
}
