package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.ExileMemberService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ExileMemberServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ExileMemberService {
    @Transactional
    override fun execute(
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
