package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.GetStudyService
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class GetStudyServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : GetStudyService {
    override fun execute(): GetStudyResponse {
        val user = currentUserProvider.getCurrentUser()
        val status = studyRedisAdapter.getApplicationStatus(user.id)
        val isBanned =
            status == StudyApplicationStatus.BANNED ||
                studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(user.id, LocalDateTime.now())
        return GetStudyResponse(
            status = status,
            currentCount = studyRedisAdapter.getCount(),
            isBanned = isBanned,
        )
    }
}
