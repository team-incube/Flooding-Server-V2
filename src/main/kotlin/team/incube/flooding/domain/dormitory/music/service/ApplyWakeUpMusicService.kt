package team.incube.flooding.domain.dormitory.music.service

import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse

interface ApplyWakeUpMusicService {
    fun execute(request: ApplyWakeUpMusicByUrlRequest): WakeUpMusicResponse
}
