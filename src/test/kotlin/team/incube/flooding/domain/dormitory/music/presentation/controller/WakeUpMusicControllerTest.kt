package team.incube.flooding.domain.dormitory.music.presentation.controller

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.CancelLikeWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.CancelWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.LikeWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.SubscribeWakeUpMusicService
import java.time.LocalDateTime

class WakeUpMusicControllerTest :
    BehaviorSpec({
        val applyWakeUpMusicService = mockk<ApplyWakeUpMusicService>()
        val cancelWakeUpMusicService = mockk<CancelWakeUpMusicService>()
        val getWakeUpMusicService = mockk<GetWakeUpMusicService>()
        val likeWakeUpMusicService = mockk<LikeWakeUpMusicService>()
        val cancelLikeWakeUpMusicService = mockk<CancelLikeWakeUpMusicService>()
        val subscribeWakeUpMusicService = mockk<SubscribeWakeUpMusicService>()

        val controller =
            WakeUpMusicController(
                applyWakeUpMusicService = applyWakeUpMusicService,
                cancelWakeUpMusicService = cancelWakeUpMusicService,
                getWakeUpMusicService = getWakeUpMusicService,
                likeWakeUpMusicService = likeWakeUpMusicService,
                cancelLikeWakeUpMusicService = cancelLikeWakeUpMusicService,
                subscribeWakeUpMusicService = subscribeWakeUpMusicService,
            )

        given("기상음악 신청이 성공할 때") {
            val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=test")
            val appliedAt = LocalDateTime.of(2026, 4, 30, 8, 0)
            val serviceResponse =
                WakeUpMusicResponse(
                    id = 1L,
                    userId = 1L,
                    userName = "테스트",
                    studentNumber = 1101,
                    musicUrl = request.musicUrl,
                    title = "테스트 음악",
                    artist = "테스트 채널",
                    duration = "PT3M21S",
                    durationText = "3:21",
                    thumbnailUrl = "https://img.youtube.com/vi/test/maxresdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=test",
                    appliedAt = appliedAt,
                    likeCount = 0,
                )
            every { applyWakeUpMusicService.execute(request) } returns serviceResponse

            `when`("컨트롤러가 응답을 만들면") {
                val response = controller.applyWakeUpMusic(request)

                then("신청된 기상음악 데이터가 응답에 포함된다") {
                    response shouldBe serviceResponse
                    verify(exactly = 1) { applyWakeUpMusicService.execute(request) }
                }
            }
        }
    })
