package team.incube.flooding.domain.dormitory.music.service

import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicRequest

interface ApplyWakeUpMusicService {
    fun execute(request: ApplyWakeUpMusicRequest)
}
