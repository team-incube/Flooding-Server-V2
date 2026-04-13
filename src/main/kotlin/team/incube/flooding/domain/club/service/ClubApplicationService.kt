package team.incube.flooding.domain.club.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class ClubApplicationService(
    private val clubJpaRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) {
    @Transactional
    fun approveApplication(
        clubId: Long,
        userId: Long,
    ) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 동아리입니다") }

        val currentUser = currentUserProvider.getCurrentUser()

        if (club.leader?.id != currentUser.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "동아리 리더만 승인할 수 있습니다")
        }

        if (clubParticipantJpaRepository.existsById(ClubParticipantId(club = clubId, user = userId))) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 유저입니다")
        }

        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다") }

        val participant =
            ClubParticipantJpaEntity(
                club = club,
                user = user,
            )
        clubParticipantJpaRepository.save(participant)
    }
}
