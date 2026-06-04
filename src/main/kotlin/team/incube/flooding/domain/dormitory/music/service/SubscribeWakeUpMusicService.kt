package team.incube.flooding.domain.dormitory.music.service

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SubscribeWakeUpMusicService {
    fun execute(): SseEmitter
}
