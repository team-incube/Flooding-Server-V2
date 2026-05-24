package team.incube.flooding.domain.user.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.repository.ClubAutonomousApplicationRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse
import team.incube.flooding.domain.user.service.GetMeService
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.LocalDateTime

@Service
class GetMeServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val clubFormSubmissionRepository: ClubFormSubmissionRepository,
    private val clubAutonomousApplicationRepository: ClubAutonomousApplicationRepository,
    private val clock: Clock,
) : GetMeService {
    @Transactional(readOnly = true)
    override fun execute(): GetMeResponse {
        val user = currentUserProvider.getCurrentUser()
        val isBanned = studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(user.id, LocalDateTime.now(clock))
        val hasClubApplication =
            clubFormSubmissionRepository.existsByUserId(user.id) ||
                clubAutonomousApplicationRepository.existsByUserId(user.id)
        return GetMeResponse.from(user, isBanned, hasClubApplication)
    }
}
