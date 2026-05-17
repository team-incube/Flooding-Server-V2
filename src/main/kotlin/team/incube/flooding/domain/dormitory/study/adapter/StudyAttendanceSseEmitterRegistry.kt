package team.incube.flooding.domain.dormitory.study.adapter

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import java.util.concurrent.CopyOnWriteArrayList

@Component
class StudyAttendanceSseEmitterRegistry {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(emitter: SseEmitter): SseEmitter {
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        return emitter
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
