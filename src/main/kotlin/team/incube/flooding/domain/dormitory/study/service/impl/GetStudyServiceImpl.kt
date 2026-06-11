package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyListResponse
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.GetStudyService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class GetStudyServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val clock: Clock,
    private val studyProperties: StudyProperties,
) : GetStudyService {
    override fun execute(): GetStudyListResponse {
        val currentUser = currentUserProvider.getCurrentUser()
        val now = LocalTime.now(clock)
        val applicantIds = studyRedisAdapter.getApplicantIds()
        val myApplicationStatus = studyRedisAdapter.getApplicationStatus(currentUser.id)
        val isMyStudyBanned =
            myApplicationStatus == StudyApplicationStatus.BANNED ||
                studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(currentUser.id, LocalDateTime.now(clock))
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
                val userMap = userRepository.findAllById(applicantIds).associateBy { it.id }
                applicantIds.mapIndexedNotNull { index, userId ->
                    val user = userMap[userId] ?: return@mapIndexedNotNull null
                    GetStudyResponse(
                        order = index + 1,
                        userId = user.id,
                        name = user.name,
                        studentNumber = user.studentNumber,
                        sex = user.sex,
                        isBanned = user.id in bannedUserIds,
                        isChecked = user.id in checkedUserIds,
                        profileImageUrl = user.profileImageUrl,
                    )
                }
            }
        return GetStudyListResponse(
            isApplicationOpen = isStudyAvailable(now),
            myApplicationStatus =
                if (isMyStudyBanned) {
                    StudyApplicationStatus.BANNED
                } else {
                    myApplicationStatus
                },
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
