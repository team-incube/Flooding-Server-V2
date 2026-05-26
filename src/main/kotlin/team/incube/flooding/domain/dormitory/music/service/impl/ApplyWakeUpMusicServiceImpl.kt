package team.incube.flooding.domain.dormitory.music.service.impl

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
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
    private val currentUserProvider: CurrentUserProvider,
    private val youtubeClient: YoutubeClient,
    private val clock: Clock,
) : ApplyWakeUpMusicService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(request: ApplyWakeUpMusicByUrlRequest): WakeUpMusicResponse {
        val user = currentUserProvider.getCurrentUser()
        log.info("기상음악 신청 시작: userId={}, role={}, musicUrl={}", user.id, user.role, request.musicUrl)

        val histories = wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(user.id)
        if (histories.isNotEmpty()) {
            log.warn("기상음악 신청 거절 - 이미 신청됨: userId={}, musicId={}", user.id, histories.first().id)
            throw ExpectedException("이미 기상음악을 신청했습니다.", HttpStatus.CONFLICT)
        }

        val videoInfo =
            youtubeClient.getVideoInfo(request.musicUrl)
                ?: run {
                    log.warn("YouTube 영상 정보 조회 실패: userId={}, musicUrl={}", user.id, request.musicUrl)
                    throw ExpectedException("YouTube 영상 정보를 가져오지 못했습니다. URL을 확인해주세요.", HttpStatus.BAD_REQUEST)
                }

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
        log.info("기상음악 신청 완료: userId={}, musicId={}, title={}", user.id, saved.id, saved.title)

        return WakeUpMusicResponse(
            id = saved.id,
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
