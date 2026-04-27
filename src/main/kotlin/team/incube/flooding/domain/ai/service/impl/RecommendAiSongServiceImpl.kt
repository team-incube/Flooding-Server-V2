package team.incube.flooding.domain.ai.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.ai.adapter.AiSongAdapter
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.request.SongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.service.RecommendAiSongService
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class RecommendAiSongServiceImpl(
    private val aiSongAdapter: AiSongAdapter,
    private val currentUserProvider: CurrentUserProvider,
    private val wakeUpMusicRepository: WakeUpMusicRepository,
) : RecommendAiSongService {
    @Transactional(readOnly = true)
    override fun execute(): RecommendAiSongResponse {
        val user = currentUserProvider.getCurrentUser()
        val history = wakeUpMusicRepository.findTop5ByUserIdOrderByAppliedAtDesc(user.id)
        if (history.isEmpty()) {
            throw ExpectedException("추천을 위한 기상송 신청 내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
        }
        val recentSongs = history.map { SongRequest(title = it.title, artist = it.artist) }
        return aiSongAdapter.recommend(RecommendAiSongRequest(recentSongs = recentSongs))
    }
}
