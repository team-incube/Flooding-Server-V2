package team.incube.flooding.domain.dormitory.study.adapter

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.study.presentation.data.response.StudyAttendanceEventResponse
import java.util.concurrent.CopyOnWriteArrayList

@Component
class StudyAttendanceSseEmitterRegistry {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(emitter: SseEmitter): SseEmitter {
        emitters.add(emitter)
        log.info("SSE emitter 등록: emitters.size={}", emitters.size)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        return emitter
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
        log.info("broadcast 진입: emitters.size={}, event={}", emitters.size, event)
        broadcastEvent("attendance", event)
    }

    @Async
    fun broadcastCancel(event: StudyAttendanceEventResponse) {
        log.info("broadcastCancel 진입: emitters.size={}, event={}", emitters.size, event)
        broadcastEvent("cancel-attendance", event)
    }

    private fun broadcastEvent(
        name: String,
        event: StudyAttendanceEventResponse,
    ) {
        log.info("broadcastEvent: name={}, emitters.size={}", name, emitters.size)
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(name)
                            .data(event),
                    )
                    log.info("send 성공: name={}", name)
                } catch (e: Exception) {
                    log.error("send 실패: name={}, event={}", name, event, e)
                    emitter.completeWithError(e)
                }
            }
        }
    }
}
