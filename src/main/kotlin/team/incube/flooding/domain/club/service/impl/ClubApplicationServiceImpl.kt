package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.ClubApplicationService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ClubApplicationServiceImpl(
    private val clubRepository: ClubRepository,
    private val userRepository: UserRepository,
    private val clubFormRepository: ClubFormRepository,
    private val clubFormSubmissionRepository: ClubFormSubmissionRepository,
    private val clubFormAnswerRepository: ClubFormAnswerRepository,
    private val clubParticipantRepository: ClubParticipantRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ClubApplicationService {
    @Transactional
    override fun execute(
        clubId: Long,
        userId: Long,
    ) {
        val club =
            clubRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("존재하지 않는 동아리입니다", HttpStatus.NOT_FOUND) }

        val currentUser = currentUserProvider.getCurrentUser()

        if (club.leader?.id != currentUser.id) {
            throw ExpectedException("동아리 리더만 승인할 수 있습니다", HttpStatus.FORBIDDEN)
        }

        if (clubParticipantRepository.existsById(ClubParticipantId(club = clubId, user = userId))) {
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
        clubParticipantRepository.save(participant)

        clubFormRepository
            .findByClubIdAndIsActiveTrue(clubId)
            ?.let { form -> clubFormSubmissionRepository.findByFormIdAndUserId(form.id, userId) }
            ?.let { submission ->
                clubFormAnswerRepository.deleteAllBySubmissionId(submission.id)
                clubFormSubmissionRepository.deleteBySubmissionId(submission.id)
            }
    }
}
