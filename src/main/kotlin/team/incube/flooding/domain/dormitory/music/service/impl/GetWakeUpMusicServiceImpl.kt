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
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay()
        return wakeUpMusicRepository.findAllWithLikeCountByDate(startOfDay, endOfDay)
    }
}
