package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.ClubMemberService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider

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
        if (clubParticipantJpaRepository.existsById(ClubParticipantId(clubId, userId))) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 동아리에 속해 있습니다.")
        }

        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "ID가 ${clubId}인 동아리를 찾을 수 없습니다.")
                }

        val currentUser = currentUserProvider.getCurrentUser()
        val isAdmin = currentUser.role == Role.ADMIN
        val isClubLeader = club.leader?.id == currentUser.id

        if (!(isAdmin || isClubLeader)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 리더 또는 관리자만 초대할 수 있습니다.")
        }

        val user =
            userRepository
                .findById(userId)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "ID가 ${userId}인 사용자를 찾을 수 없습니다.")
                }

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
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "동아리 없음") }

        if (club.leader?.id != currentUser.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음")
        }

        val isMember =
            clubParticipantJpaRepository.existsById(
                ClubParticipantId(club = clubId, user = targetUserId),
            )
        if (!isMember) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "멤버 아님")

        val targetUser =
            userRepository
                .findById(targetUserId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "대상 없음") }

        if (club.leader?.id == targetUserId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 해당 유저가 동아리 방장입니다.")
        }

        club.leader = targetUser
    }
}
