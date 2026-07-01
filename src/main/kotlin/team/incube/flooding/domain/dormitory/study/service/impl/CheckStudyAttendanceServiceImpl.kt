package team.incube.flooding.domain.dormitory.study.service.impl

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.adapter.StudyAttendanceSseEmitterRegistry
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.entity.StudyAttendanceHistoryJpaEntity
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyAttendanceHistoryJpaRepository
import team.incube.flooding.domain.dormitory.study.service.CheckStudyAttendanceService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDate

@Service
@Transactional
class CheckStudyAttendanceServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val userRepository: UserRepository,
    private val sseEmitterRegistry: StudyAttendanceSseEmitterRegistry,
    private val studyAttendanceHistoryJpaRepository: StudyAttendanceHistoryJpaRepository,
) : CheckStudyAttendanceService {
    private val log = LoggerFactory.getLogger(javaClass)

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
        val today = LocalDate.now()
        if (!studyAttendanceHistoryJpaRepository.existsByUserIdAndAttendedDate(userId, today)) {
            studyAttendanceHistoryJpaRepository.save(
                StudyAttendanceHistoryJpaEntity(user = user, attendedDate = today),
            )
        }
        log.info("checkAttendance Redis 반영 완료, broadcast 호출 직전: userId={}", userId)
        sseEmitterRegistry.broadcast(
            StudyAttendanceEventResponse(userId = user.id, name = user.name, studentNumber = user.studentNumber),
        )
        log.info("broadcast 호출 직후 (비동기 위임 완료): userId={}", userId)
    }
}
