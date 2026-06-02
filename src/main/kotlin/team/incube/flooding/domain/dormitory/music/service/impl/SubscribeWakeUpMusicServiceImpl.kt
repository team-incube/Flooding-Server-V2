package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.flooding.domain.dormitory.music.adapter.WakeUpMusicSseEmitterRegistry
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService
import team.incube.flooding.domain.dormitory.music.service.SubscribeWakeUpMusicService
import java.time.LocalDate

@Service
class SubscribeWakeUpMusicServiceImpl(
    private val getWakeUpMusicService: GetWakeUpMusicService,
    private val sseEmitterRegistry: WakeUpMusicSseEmitterRegistry,
) : SubscribeWakeUpMusicService {
    override fun execute(): SseEmitter {
        val emitter = SseEmitter(1_800_000L)
        val initList = getWakeUpMusicService.execute(LocalDate.now())
        sseEmitterRegistry.register(emitter)
        sseEmitterRegistry.sendInitialData(emitter, initList)
        return emitter
    }
}
