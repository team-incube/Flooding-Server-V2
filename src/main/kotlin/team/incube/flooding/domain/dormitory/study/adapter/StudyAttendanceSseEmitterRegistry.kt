package team.incube.flooding.domain.dormitory.study.adapter

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import java.util.concurrent.CopyOnWriteArrayList

@Component
class StudyAttendanceSseEmitterRegistry {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(emitter: SseEmitter): SseEmitter {
        registerCallbacks(emitter)
        emitters.add(emitter)
        return emitter
    }

    fun registerWithInitialSend(
        emitter: SseEmitter,
        initialSend: () -> Unit,
    ): SseEmitter {
        registerCallbacks(emitter)
        synchronized(emitter) {
            emitters.add(emitter)
            try {
                initialSend()
            } catch (e: Exception) {
                emitters.remove(emitter)
                emitter.completeWithError(e)
            }
        }
        return emitter
    }

    private fun registerCallbacks(emitter: SseEmitter) {
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
    }

    @Scheduled(fixedDelay = 30_000L)
    fun sendHeartbeat() {
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"))
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }
        }
    }

    @Async
    fun broadcast(event: StudyAttendanceEventResponse) {
        broadcastEvent("attendance", event)
    }

    @Async
    fun broadcastCancel(event: StudyAttendanceEventResponse) {
        broadcastEvent("cancel-attendance", event)
    }

    private fun broadcastEvent(
        name: String,
        event: StudyAttendanceEventResponse,
    ) {
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(name)
                            .data(event),
                    )
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }
        }
    }
}
