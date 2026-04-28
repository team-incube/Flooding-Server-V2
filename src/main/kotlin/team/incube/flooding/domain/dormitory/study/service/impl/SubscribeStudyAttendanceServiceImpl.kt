package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.service.SubscribeStudyAttendanceService
import team.incube.flooding.domain.user.repository.UserRepository

@Service
@Transactional(readOnly = true)
class SubscribeStudyAttendanceServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val sseEmitterRegistry: StudyAttendanceSseEmitterRegistry,
) : SubscribeStudyAttendanceService {
    override fun execute(): SseEmitter {
        val emitter = SseEmitter(1_800_000L)
        sseEmitterRegistry.register(emitter)

        val attendanceIds = studyRedisAdapter.getAttendanceIds()
        val initList =
            if (attendanceIds.isEmpty()) {
                emptyList()
            } else {
                userRepository
                    .findAllById(attendanceIds)
                    .sortedBy { it.studentNumber }
                    .map { StudyAttendanceEventResponse(name = it.name, studentNumber = it.studentNumber) }
            }

        emitter.send(
            SseEmitter
                .event()
                .name("init")
                .data(initList),
        )

        return emitter
    }
}
