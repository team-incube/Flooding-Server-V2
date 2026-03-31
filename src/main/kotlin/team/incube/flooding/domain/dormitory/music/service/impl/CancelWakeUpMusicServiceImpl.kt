package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.CancelWakeUpMusicService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class CancelWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CancelWakeUpMusicService {
    @Transactional
    override fun execute(musicId: Long) {
        val user = currentUserProvider.getCurrentUser()

        val music =
            wakeUpMusicRepository.findById(musicId).orElseThrow {
                throw ExpectedException("기상음악 신청 내역이 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (music.user.id != user.id && user.role != Role.DORMITORY_MANAGER && user.role != Role.ADMIN) {
            throw ExpectedException("본인의 신청만 취소할 수 있습니다.", HttpStatus.FORBIDDEN)
        }

        wakeUpMusicRepository.delete(music)
    }
}
