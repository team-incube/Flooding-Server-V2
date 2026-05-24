package team.incube.flooding.global.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.incube.flooding.domain.dormitory.study.adapter.StudyRedisAdapter

@Component
class StudyResetScheduler(
    private val studyRedisAdapter: StudyRedisAdapter,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 6 * * *")
    fun resetStudyApplications() {
        log.info("자습 신청 상태 초기화 시작")
        studyRedisAdapter.resetAll()
        log.info("자습 신청 상태 초기화 완료")
    }
}
