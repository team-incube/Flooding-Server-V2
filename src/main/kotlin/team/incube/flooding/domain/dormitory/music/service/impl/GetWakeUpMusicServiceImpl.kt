package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService

@Service
class GetWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
) : GetWakeUpMusicService {
    @Transactional(readOnly = true)
    override fun execute(): List<WakeUpMusicResponse> = wakeUpMusicRepository.findAllWithLikeCount()
}
