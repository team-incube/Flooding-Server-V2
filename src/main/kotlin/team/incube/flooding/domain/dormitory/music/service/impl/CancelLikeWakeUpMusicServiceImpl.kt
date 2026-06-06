package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.incube.flooding.domain.dormitory.music.adapter.WakeUpMusicSseEmitterRegistry
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicLikeEvent
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.service.CancelLikeWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class CancelLikeWakeUpMusicServiceImpl(
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val sseEmitterRegistry: WakeUpMusicSseEmitterRegistry,
) : CancelLikeWakeUpMusicService {
    @Transactional
    override fun execute(musicId: Long) {
        val user = currentUserProvider.getCurrentUser()

        val like =
            wakeUpMusicLikeRepository.findByUserIdAndMusicId(user.id, musicId)
                ?: throw ExpectedException("좋아요 내역이 없습니다.", HttpStatus.NOT_FOUND)

        wakeUpMusicLikeRepository.delete(like)

        val likeCount = wakeUpMusicLikeRepository.countByMusicId(musicId)

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        sseEmitterRegistry.broadcastLike(
                            WakeUpMusicLikeEvent(
                                musicId = musicId,
                                likeCount = likeCount,
                            ),
                        )
                    }
                },
            )
        } else {
            sseEmitterRegistry.broadcastLike(
                WakeUpMusicLikeEvent(
                    musicId = musicId,
                    likeCount = likeCount,
                ),
            )
        }
    }
}
