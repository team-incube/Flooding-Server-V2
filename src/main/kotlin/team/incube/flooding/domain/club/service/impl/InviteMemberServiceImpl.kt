package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.InviteMemberService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class InviteMemberServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : InviteMemberService {
    @Transactional
    override fun execute(
        clubId: Long,
        userId: Long,
    ) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("ID가 ${clubId}인 동아리를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        if (clubParticipantJpaRepository.existsById(ClubParticipantId(clubId, userId))) {
            throw ExpectedException("이미 해당 동아리에 속해 있습니다", HttpStatus.CONFLICT)
        }

        val currentUser = currentUserProvider.getCurrentUser()
        val isAdmin = currentUser.role == Role.ADMIN
        val isClubLeader = club.leader?.id == currentUser.id

        if (!(isAdmin || isClubLeader)) {
            throw ExpectedException("동아리 리더 또는 관리자만 초대할 수 있습니다", HttpStatus.FORBIDDEN)
        }

        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ExpectedException("ID가 ${userId}인 사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        clubParticipantJpaRepository.save(ClubParticipantJpaEntity(club = club, user = user))
    }
}
