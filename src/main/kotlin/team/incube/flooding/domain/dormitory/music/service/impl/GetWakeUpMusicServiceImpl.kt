package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService

@Service
class GetWakeUpMusicServiceImpl(
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val wakeUpMusicRepository: WakeUpMusicRepository,
) : GetWakeUpMusicService {

@Transactional(readOnly = true)
    override fun execute(): List<WakeUpMusicResponse> {
        return wakeUpMusicRepository.findAll().map { music ->
            WakeUpMusicResponse(
                id = music.id,
                musicUrl = music.musicUrl,
                appliedAt = music.appliedAt,
                likeCount = wakeUpMusicLikeRepository.countByMusicId(music.id)
            )
        }
    }
}