package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicRequest
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class ApplyWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ApplyWakeUpMusicService {
    @Transactional
    override fun execute(request: ApplyWakeUpMusicRequest) {
        val user = currentUserProvider.getCurrentUser()

        wakeUpMusicRepository.save(
            WakeUpMusicJpaEntity(
                user = user,
                musicUrl = request.musicUrl,
                title = request.title,
                artist = request.artist,
            ),
        )
    }
}
