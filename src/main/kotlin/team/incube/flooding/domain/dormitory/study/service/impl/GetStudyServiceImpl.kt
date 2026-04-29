package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.GetStudyService
import team.incube.flooding.domain.user.repository.UserRepository
import java.time.Clock
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class GetStudyServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val clock: Clock,
) : GetStudyService {
    override fun execute(): List<GetStudyResponse> {
        val applicantIds = studyRedisAdapter.getApplicantIds()
        if (applicantIds.isEmpty()) return emptyList()
        val bannedUserIds =
            studyBanJpaRepository
                .findAllByUserIdInAndBannedUntilAfter(applicantIds, LocalDateTime.now(clock))
                .map { it.user.id }
                .toSet()
        val checkedUserIds = studyRedisAdapter.getAttendanceIds()
        return userRepository
            .findAllById(applicantIds)
            .sortedBy { it.studentNumber }
            .map {
                GetStudyResponse(
                    userId = it.id,
                    name = it.name,
                    studentNumber = it.studentNumber,
                    sex = it.sex,
                    isBanned = it.id in bannedUserIds,
                    isChecked = it.id in checkedUserIds,
                )
            }
    }
}
