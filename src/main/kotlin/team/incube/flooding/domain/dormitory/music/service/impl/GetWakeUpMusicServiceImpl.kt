package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService
import java.time.LocalDate

@Service
class GetWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
) : GetWakeUpMusicService {
    @Transactional(readOnly = true)
    override fun execute(date: LocalDate): List<WakeUpMusicResponse> {
        val legacyStart = date.minusDays(1).atStartOfDay()
        val legacyEnd = date.atStartOfDay()
        return wakeUpMusicRepository.findAllWithLikeCountByWakeUpDate(date, legacyStart, legacyEnd)
    }
}
