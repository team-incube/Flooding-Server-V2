package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.ClubApplicationService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ClubApplicationServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ClubApplicationService {
    @Transactional
    override fun execute(
        clubId: Long,
        userId: Long,
    ) {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("존재하지 않는 동아리입니다", HttpStatus.NOT_FOUND) }

        val currentUser = currentUserProvider.getCurrentUser()

        if (club.leader?.id != currentUser.id) {
            throw ExpectedException("동아리 리더만 승인할 수 있습니다", HttpStatus.FORBIDDEN)
        }

        if (clubParticipantJpaRepository.existsById(ClubParticipantId(club = clubId, user = userId))) {
            throw ExpectedException("이미 가입된 유저입니다", HttpStatus.CONFLICT)
        }

        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ExpectedException("존재하지 않는 유저입니다", HttpStatus.NOT_FOUND) }

        val participant =
            ClubParticipantJpaEntity(
                club = club,
                user = user,
            )
        clubParticipantJpaRepository.save(participant)
    }
}
