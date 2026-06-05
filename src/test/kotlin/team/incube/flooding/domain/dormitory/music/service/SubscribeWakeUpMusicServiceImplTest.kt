package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.music.adapter.WakeUpMusicSseEmitterRegistry
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.service.impl.SubscribeWakeUpMusicServiceImpl
import java.time.LocalDateTime

class SubscribeWakeUpMusicServiceImplTest :
    BehaviorSpec({
        val getWakeUpMusicService = mockk<GetWakeUpMusicService>()
        val sseEmitterRegistry = mockk<WakeUpMusicSseEmitterRegistry>(relaxed = true)

        val service =
            SubscribeWakeUpMusicServiceImpl(
                getWakeUpMusicService = getWakeUpMusicService,
                sseEmitterRegistry = sseEmitterRegistry,
            )

        beforeEach { clearAllMocks() }

        val sampleMusicList =
            listOf(
                WakeUpMusicResponse(
                    id = 1L,
                    userId = 1L,
                    userName = "테스트",
                    studentNumber = 1101,
                    musicUrl = "https://youtube.com/watch?v=test",
                    title = "테스트 음악",
                    artist = "테스트 채널",
                    duration = "PT3M21S",
                    durationText = "3:21",
                    thumbnailUrl = null,
                    videoUrl = null,
                    appliedAt = LocalDateTime.of(2026, 6, 5, 8, 0),
                    likeCount = 0,
                ),
            )

        given("기상음악 목록이 있을 때") {
            `when`("execute를 호출하면") {
                then("SseEmitter가 반환되고 registry에 등록 및 초기 데이터 전송이 요청된다") {
                    every { getWakeUpMusicService.execute(any(), any()) } returns sampleMusicList

                    val result = service.execute()

                    result shouldNotBe null
                    verify(exactly = 1) { sseEmitterRegistry.register(result) }
                    verify(exactly = 1) { sseEmitterRegistry.sendInitialData(result, sampleMusicList) }
                }
            }
        }

        given("기상음악 목록이 없을 때") {
            `when`("execute를 호출하면") {
                then("SseEmitter가 반환되고 빈 목록으로 초기 데이터 전송이 요청된다") {
                    every { getWakeUpMusicService.execute(any(), any()) } returns emptyList()

                    val result = service.execute()

                    result shouldNotBe null
                    verify(exactly = 1) { sseEmitterRegistry.register(result) }
                    verify(exactly = 1) { sseEmitterRegistry.sendInitialData(result, emptyList()) }
                }
            }
        }

        given("SseEmitter 반환 후 registry 상호작용 순서가 올바를 때") {
            `when`("execute를 호출하면") {
                then("register 이후에 sendInitialData가 호출된다") {
                    every { getWakeUpMusicService.execute(any(), any()) } returns sampleMusicList
                    val callOrder = mutableListOf<String>()
                    every { sseEmitterRegistry.register(any()) } answers {
                        callOrder.add("register")
                        firstArg()
                    }
                    every { sseEmitterRegistry.sendInitialData(any(), any()) } answers {
                        callOrder.add("sendInitialData")
                    }

                    service.execute()

                    callOrder[0] shouldBe "register"
                    callOrder[1] shouldBe "sendInitialData"
                }
            }
        }
    })
