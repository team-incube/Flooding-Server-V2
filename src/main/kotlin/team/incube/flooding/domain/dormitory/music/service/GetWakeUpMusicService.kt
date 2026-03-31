package team.incube.flooding.domain.dormitory.music.service

import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import java.time.LocalDate

interface GetWakeUpMusicService {
    fun execute(date: LocalDate): List<WakeUpMusicResponse>
}
