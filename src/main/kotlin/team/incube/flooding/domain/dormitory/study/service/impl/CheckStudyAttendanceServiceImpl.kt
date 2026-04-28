package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.service.CheckStudyAttendanceService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class CheckStudyAttendanceServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val currentUserProvider: CurrentUserProvider,
    private val sseEmitterRegistry: StudyAttendanceSseEmitterRegistry,
) : CheckStudyAttendanceService {
    override fun execute() {
        val user = currentUserProvider.getCurrentUser()

        if (studyRedisAdapter.getApplicationStatus(user.id) != StudyApplicationStatus.APPROVED) {
            throw ExpectedException("자습을 신청하지 않은 학생입니다.", HttpStatus.NOT_FOUND)
        }

        if (studyRedisAdapter.isAttendanceChecked(user.id)) {
            throw ExpectedException("이미 자습 체크를 완료했습니다.", HttpStatus.CONFLICT)
        }

        studyRedisAdapter.checkAttendance(user.id)
        sseEmitterRegistry.broadcast(
            StudyAttendanceEventResponse(name = user.name, studentNumber = user.studentNumber),
        )
    }
}
