package team.incube.flooding.domain.dormitory.music.adapter

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicCancelledEvent
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicLikeEvent
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList

@Component
class WakeUpMusicSseEmitterRegistry(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(emitter: SseEmitter): SseEmitter {
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        emitters.add(emitter)
        log.info("SSE emitter 등록: emitters.size={}", emitters.size)
        return emitter
    }

    @Async
    fun sendInitialData(
        emitter: SseEmitter,
        initList: List<WakeUpMusicResponse>,
    ) {
        val json =
            try {
                objectMapper.writeValueAsString(initList)
            } catch (e: Exception) {
                log.error("SSE 초기 데이터 직렬화 실패", e)
                emitters.remove(emitter)
                emitter.completeWithError(e)
                return
            }

        synchronized(emitter) {
            try {
                emitter.send(SseEmitter.event().name("connect").data("connected"))
                emitter.send(
                    SseEmitter
                        .event()
                        .name("init")
                        .data(json),
                )
            } catch (e: Exception) {
                log.error("SSE 초기 데이터 전송 실패", e)
                emitters.remove(emitter)
                emitter.completeWithError(e)
            }
        }
    }

    @Async
    fun broadcast(event: WakeUpMusicResponse) {
        log.info("broadcast 진입: emitters.size={}, event={}", emitters.size, event)
        val json =
            try {
                objectMapper.writeValueAsString(event)
            } catch (e: Exception) {
                return
            }
        broadcastEvent("music-applied", json)
    }

    @Async
    fun broadcastCancelled(event: WakeUpMusicCancelledEvent) {
        log.info("broadcastCancelled 진입: emitters.size={}, event={}", emitters.size, event)
        val json =
            try {
                objectMapper.writeValueAsString(event)
            } catch (e: Exception) {
                return
            }
        broadcastEvent("music-cancelled", json)
    }

    @Async
    fun broadcastLike(event: WakeUpMusicLikeEvent) {
        log.info("broadcastLike 진입: emitters.size={}, event={}", emitters.size, event)
        val json =
            try {
                objectMapper.writeValueAsString(event)
            } catch (e: Exception) {
                return
            }
        broadcastEvent("music-like-updated", json)
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

    private fun broadcastEvent(
        name: String,
        json: String,
    ) {
        log.info("broadcastEvent: name={}, emitters.size={}", name, emitters.size)
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(name)
                            .data(json),
                    )
                    log.info("send 성공: name={}", name)
                } catch (e: Exception) {
                    log.error("send 실패: name={}, json={}", name, json, e)
                    emitter.completeWithError(e)
                }
            }
        }
    }
}
