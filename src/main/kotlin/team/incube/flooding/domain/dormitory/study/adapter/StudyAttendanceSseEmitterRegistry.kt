package team.incube.flooding.domain.dormitory.study.adapter

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList

@Component
class StudyAttendanceSseEmitterRegistry(
    private val objectMapper: ObjectMapper,
) {
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
        val json =
            try {
                objectMapper.writeValueAsString(event)
            } catch (e: Exception) {
                return
            }
        broadcastEvent("attendance", json)
    }

    @Async
    fun broadcastCancel(event: StudyAttendanceEventResponse) {
        val json =
            try {
                objectMapper.writeValueAsString(event)
            } catch (e: Exception) {
                return
            }
        broadcastEvent("cancel-attendance", json)
    }

    private fun broadcastEvent(
        name: String,
        json: String,
    ) {
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(name)
                            .data(json),
                    )
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }
        }
    }
}
