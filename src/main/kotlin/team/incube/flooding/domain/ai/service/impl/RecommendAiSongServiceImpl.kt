package team.incube.flooding.domain.ai.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.ai.adapter.AiSongAdapter
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.request.SongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.service.RecommendAiSongService
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class RecommendAiSongServiceImpl(
    private val aiSongAdapter: AiSongAdapter,
    private val currentUserProvider: CurrentUserProvider,
    private val wakeUpMusicRepository: WakeUpMusicRepository,
) : RecommendAiSongService {
    @Transactional(readOnly = true)
    override fun execute(): RecommendAiSongResponse {
        val user = currentUserProvider.getCurrentUser()
        val recentSongs =
            wakeUpMusicRepository
                .findTop5ByUserIdOrderByAppliedAtDesc(user.id)
                .map { SongRequest(title = it.title, artist = it.artist) }
        return aiSongAdapter.recommend(RecommendAiSongRequest(recentSongs = recentSongs))
    }
}
