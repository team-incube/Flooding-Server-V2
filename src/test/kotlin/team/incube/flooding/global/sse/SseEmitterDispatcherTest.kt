package team.incube.flooding.global.sse

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.longs.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class SseEmitterDispatcherTest :
    BehaviorSpec({
        val dispatcher = SseEmitterDispatcher(Executors.newVirtualThreadPerTaskExecutor())

        given("느린 emitter와 정상 emitter가 함께 있을 때") {
            `when`("dispatch를 호출하면") {
                then("호출 스레드를 막지 않고, 느린 연결만 제거되며 정상 연결은 유지된다") {
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

                    elapsed shouldBeLessThan 1_000L

                    Thread.sleep(3_500)

                    verify(exactly = 1) { healthy.send(any<SseEmitter.SseEventBuilder>()) }
                    verify(exactly = 0) { healthy.completeWithError(any()) }
                    verify(exactly = 1) { slow.completeWithError(any()) }
                    emitters shouldContain healthy
                    emitters shouldNotContain slow
                }
            }
        }
    })
