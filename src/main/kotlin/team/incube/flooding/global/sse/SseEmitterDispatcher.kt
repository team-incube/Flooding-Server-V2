package team.incube.flooding.global.sse

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@Component
class SseEmitterDispatcher(
    @Qualifier("sseSendExecutor") private val sendExecutor: Executor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun dispatch(
        emitters: CopyOnWriteArrayList<SseEmitter>,
        event: () -> SseEmitter.SseEventBuilder,
    ) {
        emitters.toList().forEach { emitter ->
            CompletableFuture
                .runAsync({
                    synchronized(emitter) { emitter.send(event()) }
                }, sendExecutor)
                .orTimeout(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally { e ->
                    if (emitters.remove(emitter)) {
                        log.warn("SSE 전송 실패/지연으로 emitter 제거: {}", e.message)
                        runCatching { emitter.completeWithError(e) }
                    }
                    null
                }
        }
    }

    companion object {
        private const val SEND_TIMEOUT_MS = 3_000L
    }
}
