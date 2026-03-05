package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.StudyApplicationService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class StudyApplicationServiceImpl (
    private val studyRedisAdapter: StudyRedisAdapter,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val studyProperties: StudyProperties
) : StudyApplicationService {

    override fun execute() {

        val user = currentUserProvider.getCurrentUser()

        val now = LocalTime.now()

        if(now.isBefore(studyProperties.openTime) || now.isAfter(studyProperties.closeTime)) {
            throw ExpectedException("자습 신청 시간이 아닙니다.", HttpStatus.BAD_REQUEST)
        }
        val status = studyRedisAdapter.getApplicationStatus(user.id)
        if(status == StudyApplicationStatus.BANNED ||
            studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(user.id, LocalDateTime.now())) {
            throw ExpectedException("자습 금지 상태입니다.", HttpStatus.FORBIDDEN)
        }
        if(status == StudyApplicationStatus.APPROVED) {
            throw ExpectedException("이미 자습을 신청했습니다.", HttpStatus.CONFLICT)
        }
        if(status == StudyApplicationStatus.CANCELLED) {
            throw ExpectedException("자습을 취소하여 신청하지 못합니다.", HttpStatus.CONFLICT)
        }

        val newCount = studyRedisAdapter.incrementCount()
        if(newCount > studyProperties.maxCount){
            studyRedisAdapter.decrementCount()
            throw ExpectedException("자습 신청 인원이 마감되었습니다.", HttpStatus.CONFLICT)
        }

        studyRedisAdapter.saveApplication(user.id, StudyApplicationStatus.APPROVED)
    }
}
