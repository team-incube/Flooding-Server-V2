package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyListResponse
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.GetStudyService
import team.incube.flooding.domain.user.repository.UserRepository
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class GetStudyServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val clock: Clock,
    private val studyProperties: StudyProperties,
) : GetStudyService {
    override fun execute(): GetStudyListResponse {
        val now = LocalTime.now(clock)
        val applicantIds = studyRedisAdapter.getApplicantIds()
        val bannedUserIds =
            if (applicantIds.isEmpty()) {
                emptySet()
            } else {
                studyBanJpaRepository
                    .findAllByUserIdInAndBannedUntilAfter(applicantIds, LocalDateTime.now(clock))
                    .map { it.user.id }
                    .toSet()
            }
        val checkedUserIds = studyRedisAdapter.getAttendanceIds()
        val applicants =
            if (applicantIds.isEmpty()) {
                emptyList()
            } else {
                userRepository
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
        return GetStudyListResponse(
            isApplicationOpen = isStudyAvailable(now),
            applicants = applicants,
        )
    }

    private fun isStudyAvailable(now: LocalTime): Boolean {
        val crossesMidnight = studyProperties.openTime.isAfter(studyProperties.closeTime)
        val outOfRange =
            if (crossesMidnight) {
                now.isBefore(studyProperties.openTime) && now.isAfter(studyProperties.closeTime)
            } else {
                now.isBefore(studyProperties.openTime) || now.isAfter(studyProperties.closeTime)
            }
        return !outOfRange
    }
}
