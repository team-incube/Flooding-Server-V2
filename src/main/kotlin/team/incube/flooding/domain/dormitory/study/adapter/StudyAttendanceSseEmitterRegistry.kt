package team.incube.flooding.domain.dormitory.study.adapter

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

    fun broadcast(event: StudyAttendanceEventResponse) {
        val deadEmitters = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter
                        .event()
                        .name("attendance")
                        .data(event),
                )
            } catch (e: Exception) {
                deadEmitters.add(emitter)
            }
        }
        emitters.removeAll(deadEmitters)
    }
}
