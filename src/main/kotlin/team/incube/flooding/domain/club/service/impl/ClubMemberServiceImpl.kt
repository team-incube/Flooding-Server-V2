package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.ClubMemberService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ClubMemberServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ClubMemberService {
    @Transactional
    override fun inviteMember(
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

        val newParticipant =
            ClubParticipantJpaEntity(
                club = club,
                user = user,
            )

        clubParticipantJpaRepository.save(newParticipant)
    }

    @Transactional
    override fun transferOwnerShip(
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

        val isMember =
            clubParticipantJpaRepository.existsById(
                ClubParticipantId(club = clubId, user = targetUserId),
            )
        if (!isMember) throw ExpectedException("동아리 멤버가 아닙니다", HttpStatus.BAD_REQUEST)

        val targetUser =
            userRepository
                .findById(targetUserId)
                .orElseThrow { ExpectedException("대상 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        if (club.leader?.id == targetUserId) {
            throw ExpectedException("이미 해당 유저가 동아리 방장입니다", HttpStatus.BAD_REQUEST)
        }

        club.leader = targetUser
    }

    @Transactional
    override fun exileMember(
        clubId: Long,
        userId: Long,
    ) {
        val currentUser = currentUserProvider.getCurrentUser()

        if (currentUser.role != Role.ADMIN) {
            throw ExpectedException("관리자만 구성원을 추방할 수 있습니다", HttpStatus.FORBIDDEN)
        }

        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다", HttpStatus.NOT_FOUND) }

        if (club.leader?.id == userId) {
            throw ExpectedException("동아리 방장은 추방할 수 없습니다", HttpStatus.BAD_REQUEST)
        }

        val participantId = ClubParticipantId(club = clubId, user = userId)
        if (!clubParticipantJpaRepository.existsById(participantId)) {
            throw ExpectedException("해당 동아리에 소속된 유저가 아닙니다", HttpStatus.NOT_FOUND)
        }

        clubParticipantJpaRepository.deleteById(participantId)
    }
}
