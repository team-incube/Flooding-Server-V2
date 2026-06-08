package team.incube.flooding.domain.dormitory.music.adapter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicCancelledEvent
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicLikeEvent
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import tools.jackson.databind.ObjectMapper
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList

class WakeUpMusicSseEmitterRegistryTest :
    BehaviorSpec({
        val objectMapper = mockk<ObjectMapper>()
        lateinit var registry: WakeUpMusicSseEmitterRegistry

        fun emitters(): CopyOnWriteArrayList<SseEmitter> {
            val field = WakeUpMusicSseEmitterRegistry::class.java.getDeclaredField("emitters")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return field.get(registry) as CopyOnWriteArrayList<SseEmitter>
        }

        val sampleMusic =
            WakeUpMusicResponse(
                id = 1L,
                userId = 1L,
                userName = "테스트",
                studentNumber = 1101,
                musicUrl = "https://youtube.com/watch?v=test",
                title = "테스트 음악",
                artist = "테스트 채널",
                duration = "PT3M",
                durationText = "3:00",
                thumbnailUrl = null,
                videoUrl = null,
                appliedAt = LocalDateTime.of(2026, 6, 5, 8, 0),
                likeCount = 0,
            )

        beforeEach {
            clearAllMocks()
            registry = WakeUpMusicSseEmitterRegistry(objectMapper)
        }

        given("emitter를 등록할 때") {
            `when`("register를 호출하면") {
                then("emitter가 목록에 추가된다") {
                    registry.register(SseEmitter(1_800_000L))

                    emitters() shouldHaveSize 1
                }
            }
        }

        given("초기 데이터를 전송할 때") {
            val initList = listOf(sampleMusic)

            `when`("직렬화가 성공하면") {
                then("connect와 init 이벤트 총 2회 전송되고 연결이 유지된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { objectMapper.writeValueAsString(any()) } returns "[]"

                    registry.sendInitialData(emitter, initList)

                    verify(exactly = 2) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
                    // 성공 시 emitter가 목록에서 제거되지 않아야 한다 (SSE 연결 유지)
                    emitters() shouldHaveSize 1
                }
            }

            `when`("직렬화가 실패하면") {
                then("emitter가 목록에서 제거되고 에러 완료가 호출된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { objectMapper.writeValueAsString(any()) } answers {
                        throw RuntimeException("직렬화 실패")
                    }

                    registry.sendInitialData(emitter, initList)

                    emitters().shouldBeEmpty()
                    verify(exactly = 1) { emitter.completeWithError(any()) }
                }
            }

            `when`("이벤트 전송 중 IOException이 발생하면") {
                then("emitter가 목록에서 제거되고 에러 완료가 호출된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { objectMapper.writeValueAsString(any()) } returns "[]"
                    every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")

                    registry.sendInitialData(emitter, initList)

                    emitters().shouldBeEmpty()
                    verify(exactly = 1) { emitter.completeWithError(any()) }
                }
            }
        }

        given("broadcast를 호출할 때") {
            `when`("등록된 emitter가 여러 개 있으면") {
                then("모든 emitter에 이벤트가 전송된다") {
                    val emitter1 = spyk(SseEmitter(1_800_000L))
                    val emitter2 = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter1)
                    registry.register(emitter2)
                    every { objectMapper.writeValueAsString(sampleMusic) } returns "{}"

                    registry.broadcast(sampleMusic)

                    verify(exactly = 1) { emitter1.send(any<SseEmitter.SseEventBuilder>()) }
                    verify(exactly = 1) { emitter2.send(any<SseEmitter.SseEventBuilder>()) }
                }
            }

            `when`("특정 emitter 전송이 실패하면") {
                then("해당 emitter에만 에러 완료가 호출된다") {
                    val failingEmitter = spyk(SseEmitter(1_800_000L))
                    val healthyEmitter = spyk(SseEmitter(1_800_000L))
                    registry.register(failingEmitter)
                    registry.register(healthyEmitter)
                    every { objectMapper.writeValueAsString(sampleMusic) } returns "{}"
                    every { failingEmitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")

                    registry.broadcast(sampleMusic)

                    verify(exactly = 1) { failingEmitter.completeWithError(any()) }
                    verify(exactly = 0) { healthyEmitter.completeWithError(any()) }
                }
            }
        }

        given("broadcastCancelled를 호출할 때") {
            val cancelEvent = WakeUpMusicCancelledEvent(musicId = 1L)

            `when`("등록된 emitter가 있으면") {
                then("emitter에 취소 이벤트가 전송된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { objectMapper.writeValueAsString(cancelEvent) } returns "{}"

                    registry.broadcastCancelled(cancelEvent)

                    verify(exactly = 1) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
                }
            }
        }

        given("broadcastLike를 호출할 때") {
            val likeEvent = WakeUpMusicLikeEvent(musicId = 1L, likeCount = 5L)

            `when`("등록된 emitter가 있으면") {
                then("emitter에 좋아요 업데이트 이벤트가 전송된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { objectMapper.writeValueAsString(likeEvent) } returns "{}"

                    registry.broadcastLike(likeEvent)

                    verify(exactly = 1) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
                }
            }
        }

        given("heartbeat를 전송할 때") {
            `when`("전송이 성공하면") {
                then("모든 emitter에 heartbeat comment가 전송된다") {
                    val emitter1 = spyk(SseEmitter(1_800_000L))
                    val emitter2 = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter1)
                    registry.register(emitter2)

                    registry.sendHeartbeat()

                    verify(exactly = 1) { emitter1.send(any<SseEmitter.SseEventBuilder>()) }
                    verify(exactly = 1) { emitter2.send(any<SseEmitter.SseEventBuilder>()) }
                }
            }

            `when`("전송이 실패하면") {
                then("해당 emitter에 에러 완료가 호출된다") {
                    val emitter = spyk(SseEmitter(1_800_000L))
                    registry.register(emitter)
                    every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")

                    registry.sendHeartbeat()

                    verify(exactly = 1) { emitter.completeWithError(any()) }
                }
            }
        }
    })
