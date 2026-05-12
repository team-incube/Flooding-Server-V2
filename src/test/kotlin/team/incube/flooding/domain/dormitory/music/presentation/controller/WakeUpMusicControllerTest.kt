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
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class WakeUpMusicControllerTest :
    BehaviorSpec({
        val applyWakeUpMusicService = mockk<ApplyWakeUpMusicService>()
        val cancelWakeUpMusicService = mockk<CancelWakeUpMusicService>()
        val getWakeUpMusicService = mockk<GetWakeUpMusicService>()
        val likeWakeUpMusicService = mockk<LikeWakeUpMusicService>()
        val cancelLikeWakeUpMusicService = mockk<CancelLikeWakeUpMusicService>()
        val clock =
            Clock.fixed(
                Instant.parse("2026-05-10T15:30:00Z"),
                ZoneId.of("Asia/Seoul"),
            )

        val controller =
            WakeUpMusicController(
                applyWakeUpMusicService = applyWakeUpMusicService,
                cancelWakeUpMusicService = cancelWakeUpMusicService,
                getWakeUpMusicService = getWakeUpMusicService,
                likeWakeUpMusicService = likeWakeUpMusicService,
                cancelLikeWakeUpMusicService = cancelLikeWakeUpMusicService,
                clock = clock,
            )

        given("기상음악 신청이 성공할 때") {
            val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=test")
            val appliedAt = LocalDateTime.of(2026, 4, 30, 8, 0)
            val serviceResponse =
                WakeUpMusicResponse(
                    id = 1L,
                    musicUrl = request.musicUrl,
                    appliedAt = appliedAt,
                    likeCount = 0,
                )
            every { applyWakeUpMusicService.execute(request) } returns serviceResponse

            `when`("컨트롤러가 응답을 만들면") {
                val response = controller.applyWakeUpMusic(request)

                then("신청된 기상음악 데이터가 응답에 포함된다") {
                    response.status shouldBe HttpStatus.OK
                    response.code shouldBe 200
                    response.message shouldBe "OK"
                    response.data shouldBe serviceResponse
                    verify(exactly = 1) { applyWakeUpMusicService.execute(request) }
                }
            }
        }

        given("기상음악 목록 조회 시 날짜가 전달되지 않을 때") {
            val serviceResponse = emptyList<WakeUpMusicResponse>()
            every { getWakeUpMusicService.execute(LocalDate.of(2026, 5, 11)) } returns serviceResponse

            `when`("컨트롤러가 기본 날짜로 목록을 조회하면") {
                val response = controller.getWakeUpMusic(null)

                then("Clock 기준 오늘 날짜로 조회한다") {
                    response.status shouldBe HttpStatus.OK
                    response.code shouldBe 200
                    response.message shouldBe "OK"
                    response.data shouldBe serviceResponse
                    verify(exactly = 1) { getWakeUpMusicService.execute(LocalDate.of(2026, 5, 11)) }
                }
            }
        }
    })
