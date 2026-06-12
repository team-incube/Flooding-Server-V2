package team.incube.flooding.global.sse

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.longs.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadPoolExecutor
import kotlin.system.measureTimeMillis

class SseEmitterDispatcherTest :
    BehaviorSpec({
        val sseSendExecutor =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 4
                maxPoolSize = 8
                queueCapacity = 100
                setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
                initialize()
            }
        val dispatcher = SseEmitterDispatcher(sseSendExecutor)

        given("느린 emitter와 정상 emitter가 함께 있을 때") {
            `when`("dispatch를 호출하면") {
                then("느린 연결이 정상 연결의 수신을 막지 않고, 느린 연결만 제거된다") {
                    val healthy = mockk<SseEmitter>(relaxed = true)
                    val slow = mockk<SseEmitter>(relaxed = true)
                    every { slow.send(any<SseEmitter.SseEventBuilder>()) } answers {
                        Thread.sleep(5_000)
                    }

                    val emitters = CopyOnWriteArrayList(listOf(healthy, slow))

                    val elapsed =
                        measureTimeMillis {
                            dispatcher.dispatch(emitters) {
                                SseEmitter.event().comment("heartbeat")
                            }
                        }

                    elapsed shouldBeLessThan 5_000L
                    verify(exactly = 1) { healthy.send(any<SseEmitter.SseEventBuilder>()) }
                    verify(exactly = 0) { healthy.completeWithError(any()) }
                    verify(exactly = 1) { slow.completeWithError(any()) }
                    emitters shouldContain healthy
                    emitters shouldNotContain slow
                }
            }
        }
    })
