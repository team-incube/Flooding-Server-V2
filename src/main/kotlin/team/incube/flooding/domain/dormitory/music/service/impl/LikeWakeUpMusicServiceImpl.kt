package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicLikeJpaEntity
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.LikeWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class LikeWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val wakeUpMusicLikeRepository: WakeUpMusicLikeRepository,
    private val currentUserProvider: CurrentUserProvider,
) : LikeWakeUpMusicService {
    @Transactional
    override fun execute(musicId: Long) {
        val user = currentUserProvider.getCurrentUser()

        val music =
            wakeUpMusicRepository.findById(musicId).orElseThrow {
                ExpectedException("기상음악 신청 내역이 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (wakeUpMusicLikeRepository.existsByUserIdAndMusicId(user.id, musicId)) {
            throw ExpectedException("이미 좋아요를 눌렀습니다.", HttpStatus.CONFLICT)
        }

        wakeUpMusicLikeRepository.save(
            WakeUpMusicLikeJpaEntity(
                user = user,
                music = music,
            ),
        )
    }
}
