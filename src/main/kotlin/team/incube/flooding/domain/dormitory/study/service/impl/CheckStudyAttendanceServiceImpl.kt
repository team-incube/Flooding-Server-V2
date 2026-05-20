package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.service.CheckStudyAttendanceService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional(readOnly = true)
class CheckStudyAttendanceServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val sseEmitterRegistry: StudyAttendanceSseEmitterRegistry,
) : CheckStudyAttendanceService {
    override fun execute(userId: Long) {
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ExpectedException("존재하지 않는 학생입니다.", HttpStatus.NOT_FOUND) }

        if (studyRedisAdapter.getApplicationStatus(userId) != StudyApplicationStatus.APPROVED) {
            throw ExpectedException("자습을 신청하지 않은 학생입니다.", HttpStatus.NOT_FOUND)
        }

        if (studyRedisAdapter.isAttendanceChecked(userId)) {
            throw ExpectedException("이미 자습 체크를 완료했습니다.", HttpStatus.CONFLICT)
        }

        studyRedisAdapter.checkAttendance(userId)
        sseEmitterRegistry.broadcast(
            StudyAttendanceEventResponse(userId = user.id, name = user.name, studentNumber = user.studentNumber),
        )
    }
}
