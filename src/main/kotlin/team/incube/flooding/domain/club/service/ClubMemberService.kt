package team.incube.flooding.domain.club.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class ClubMemberService(
    private val clubRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) {
    @Transactional
    fun inviteMember(
        clubId: Long,
        userId: Long,
    ) {

        if (clubParticipantJpaRepository.existsById(ClubParticipantId(clubId, userId))) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 동아리에 속해 있습니다.")
        }

        val club = clubRepository.findById(clubId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "ID가 ${clubId}인 동아리를 찾을 수 없습니다.")
            }

        val currentUser = currentUserProvider.getCurrentUser()
        if (club.leader?.id != currentUser.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 리더만 멤버를 초대할 수 있습니다.")
        }

        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "ID가 ${userId}인 사용자를 찾을 수 없습니다.")
            }

        val newParticipant = ClubParticipantJpaEntity(
            club = club,
            user = user,
        )

        clubParticipantJpaRepository.save(newParticipant)
    }
}