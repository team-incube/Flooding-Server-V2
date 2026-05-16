package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.DeleteClubService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubParticipantRepository: ClubParticipantRepository,
    private val clubFormRepository: ClubFormRepository,
    private val clubFormFieldRepository: ClubFormFieldRepository,
    private val clubFormFieldOptionRepository: ClubFormFieldOptionRepository,
    private val clubFormSubmissionRepository: ClubFormSubmissionRepository,
    private val clubFormAnswerRepository: ClubFormAnswerRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteClubService {
    @Transactional
    override fun execute(clubId: Long) {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }

        val currentUser = currentUserProvider.getCurrentUser()

        if (!club.isModifiableBy(currentUser)) {
            throw ExpectedException("동아리를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        clubFormAnswerRepository.deleteAllByClubId(clubId)
        clubFormSubmissionRepository.deleteAllByClubId(clubId)
        clubFormFieldOptionRepository.deleteAllByClubId(clubId)
        clubFormFieldRepository.deleteAllByClubId(clubId)
        clubFormRepository.deleteAllByClubId(clubId)
        clubParticipantRepository.deleteAllByClubId(clubId)
        clubRepository.delete(club)
    }
}
