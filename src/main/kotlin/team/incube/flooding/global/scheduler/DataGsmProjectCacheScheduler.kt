package team.incube.flooding.global.scheduler

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.global.client.DataGsmProjectClient

@Component
class DataGsmProjectCacheScheduler(
    private val clubRepository: ClubRepository,
    private val dataGsmProjectClient: DataGsmProjectClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun warmUpOnStartup() = warmAll()

    @Scheduled(fixedRate = 30 * 60 * 1000)
    fun warmUpScheduled() = warmAll()

    private fun warmAll() {
        val clubs = clubRepository.findAllByDataGsmClubIdIsNotNull()
        log.info("DataGSM 프로젝트 캐시 워밍 시작: {}개 동아리", clubs.size)
        clubs.forEach { club ->
            runCatching { dataGsmProjectClient.warmCache(club.dataGsmClubId!!) }
                .onFailure { log.error("캐시 워밍 실패: clubId=${club.id}", it) }
        }
        log.info("DataGSM 프로젝트 캐시 워밍 완료")
    }
}
