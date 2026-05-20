package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.service.UncheckStudyAttendanceService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class UncheckStudyAttendanceServiceImpl(
    private val userRepository: UserRepository,
    private val studyRedisAdapter: StudyRedisAdapter,
    private val sseEmitterRegistry: StudyAttendanceSseEmitterRegistry,
) : UncheckStudyAttendanceService {
    override fun execute(userId: Long) {
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ExpectedException("존재하지 않는 학생입니다.", HttpStatus.NOT_FOUND) }

        if (!studyRedisAdapter.isAttendanceChecked(userId)) {
            throw ExpectedException("체크인 기록이 없습니다.", HttpStatus.NOT_FOUND)
        }

        studyRedisAdapter.cancelAttendance(userId)
        sseEmitterRegistry.broadcastCancel(
            StudyAttendanceEventResponse(userId = user.id, name = user.name, studentNumber = user.studentNumber),
        )
    }
}
