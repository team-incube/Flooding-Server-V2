package team.incube.flooding.domain.dormitory.music.service

import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse

interface GetWakeUpMusicService {
    fun execute(): List<WakeUpMusicResponse>
}
