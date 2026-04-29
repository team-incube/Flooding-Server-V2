package team.incube.flooding.domain.dormitory.music.service

import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest

interface ApplyWakeUpMusicService {
    fun execute(request: ApplyWakeUpMusicByUrlRequest)
}
