package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter
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
    private val currentUserProvider: CurrentUserProvider
) : StudyApplicationService {

    override fun execute() {

        var user = currentUserProvider.getCurrentUser()

        val now = LocalTime.now()
        val opentime = LocalTime.of(20,0)
        val closetime = LocalTime.of(21,0)

        if(now.isBefore(opentime) || now.isAfter(closetime)) {
            throw ExpectedException("자습 신청 시간이 아닙니다.", HttpStatus.BAD_REQUEST)
        }
        if(studyBanJpaRepository.existsByUserAndBannedUntilAfter(user, LocalDateTime.now())) {
            throw ExpectedException("자습 금지 상태입니다.", HttpStatus.FORBIDDEN)
        }
        val status = studyRedisAdapter.getApplicationStatus(user.id)
        if(status == StudyApplicationStatus.APPROVED || status == StudyApplicationStatus.CANCELLED) {
            throw ExpectedException("이미 자습을 신청했습니다.", HttpStatus.CONFLICT)
        }

        if(studyRedisAdapter.getCount()>=50){
            throw ExpectedException("자습 신청 인원이 마감되었습니다.", HttpStatus.CONFLICT)
        }

        studyRedisAdapter.saveApplication(user.id ,StudyApplicationStatus.APPROVED)
        studyRedisAdapter.incrementCount()
    }
}