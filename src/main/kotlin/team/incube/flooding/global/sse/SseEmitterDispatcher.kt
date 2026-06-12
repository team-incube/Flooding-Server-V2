package team.incube.flooding.global.sse

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Component
class SseEmitterDispatcher(
    @Qualifier("sseSendExecutor") private val sendExecutor: ThreadPoolTaskExecutor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun dispatch(
        emitters: CopyOnWriteArrayList<SseEmitter>,
        event: () -> SseEmitter.SseEventBuilder,
    ) {
        val targets = emitters.toList()
        if (targets.isEmpty()) return

        val tasks =
            targets.map { emitter ->
                Callable {
                    synchronized(emitter) { emitter.send(event()) }
                }
            }

        val futures =
            sendExecutor.threadPoolExecutor.invokeAll(tasks, SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS)

        futures.forEachIndexed { index, future ->
            try {
                future.get()
            } catch (e: Exception) {
                val emitter = targets[index]
                if (emitters.remove(emitter)) {
                    log.warn("SSE 전송 실패/지연으로 emitter 제거: {}", e.message)
                    runCatching { emitter.completeWithError(e) }
                }
            }
        }
    }

    companion object {
        private const val SEND_TIMEOUT_MS = 3_000L
    }
}
