package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.presentation.data.request.WakeUpMusicSort
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.GetWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.LocalDate

@Service
class GetWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val currentUserProvider: CurrentUserProvider,
) : GetWakeUpMusicService {
    @Transactional(readOnly = true)
    override fun execute(
        date: LocalDate,
        sort: WakeUpMusicSort,
    ): List<WakeUpMusicResponse> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay()
        val musicList = wakeUpMusicRepository.findAllWithLikeCountByDate(startOfDay, endOfDay)
        if (musicList.isEmpty()) return emptyList()
        val currentUser = currentUserProvider.getCurrentUser()
        val likedIds =
            wakeUpMusicLikeRepository
                .findLikedMusicIdsByUserIdAndMusicIdIn(currentUser.id, musicList.map { it.id })
                .toSet()
        val result = musicList.map { it.copy(isLiked = it.id in likedIds) }
        return when (sort) {
            WakeUpMusicSort.TIME -> result.sortedByDescending { it.appliedAt }
            WakeUpMusicSort.LIKE -> result.sortedByDescending { it.likeCount }
        }
    }
}
