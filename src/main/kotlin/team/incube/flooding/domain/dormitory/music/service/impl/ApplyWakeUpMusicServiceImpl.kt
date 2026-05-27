package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.global.client.YoutubeClient
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.Clock
import java.time.LocalDateTime

@Service
class ApplyWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val youtubeClient: YoutubeClient,
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

        val videoInfo =
            youtubeClient.getVideoInfo(request.musicUrl)
                ?: throw ExpectedException("YouTube 영상 정보를 가져오지 못했습니다. URL을 확인해주세요.", HttpStatus.BAD_REQUEST)

        val saved =
            wakeUpMusicRepository.save(
                WakeUpMusicJpaEntity(
                    user = user,
                    musicUrl = videoInfo.videoUrl,
                    title = videoInfo.title,
                    artist = videoInfo.artist,
                    duration = videoInfo.duration,
                    durationText = videoInfo.durationText,
                    thumbnailUrl = videoInfo.thumbnailUrl,
                    videoUrl = videoInfo.videoUrl,
                    appliedAt = LocalDateTime.now(clock),
                ),
            )

        return WakeUpMusicResponse(
            id = saved.id,
            userId = user.id,
            userName = user.name,
            studentNumber = user.studentNumber,
            musicUrl = saved.musicUrl,
            title = saved.title,
            artist = saved.artist,
            duration = saved.duration,
            durationText = saved.durationText,
            thumbnailUrl = saved.thumbnailUrl,
            videoUrl = saved.videoUrl,
            appliedAt = saved.appliedAt,
            likeCount = 0,
            isLiked = false,
        )
    }
}
