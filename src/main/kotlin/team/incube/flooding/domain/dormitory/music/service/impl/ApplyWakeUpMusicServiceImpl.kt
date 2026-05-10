package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.LocalDateTime

@Service
class ApplyWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val clock: Clock,
) : ApplyWakeUpMusicService {
    companion object {
        private const val MAX_HISTORY_SIZE = 5
    }

    @Transactional
    override fun execute(request: ApplyWakeUpMusicByUrlRequest): WakeUpMusicResponse {
        val user = currentUserProvider.getCurrentUser()

        val histories = wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(user.id)
        if (histories.size >= MAX_HISTORY_SIZE) {
            val toDelete = histories.take(histories.size - MAX_HISTORY_SIZE + 1)
            wakeUpMusicLikeRepository.deleteAllByMusicIdIn(toDelete.map { it.id })
            wakeUpMusicRepository.deleteAllInBatch(toDelete)
        }

        val saved =
            wakeUpMusicRepository.save(
                WakeUpMusicJpaEntity(
                    user = user,
                    musicUrl = request.musicUrl,
                    appliedAt = LocalDateTime.now(clock),
                ),
            )

        return WakeUpMusicResponse(
            id = saved.id,
            musicUrl = saved.musicUrl,
            appliedAt = saved.appliedAt,
            likeCount = 0,
        )
    }
}
