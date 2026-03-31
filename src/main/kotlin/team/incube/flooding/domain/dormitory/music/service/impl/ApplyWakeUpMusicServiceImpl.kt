package team.incube.flooding.domain.dormitory.music.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicRequest
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.ApplyWakeUpMusicService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ApplyWakeUpMusicServiceImpl(
    private val wakeUpMusicRepository: WakeUpMusicRepository,
    private val currentUserProvider: CurrentUserProvider
) : ApplyWakeUpMusicService {

    @Transactional
    override fun execute(request: ApplyWakeUpMusicRequest) {
        val user = currentUserProvider.getCurrentUser()

        if (wakeUpMusicRepository.existsByUserId(user.id)) {
            throw ExpectedException("이미 기상음악을 신청했습니다.", HttpStatus.CONFLICT)
        }

        wakeUpMusicRepository.save(
            WakeUpMusicJpaEntity(
                user = user,
                musicUrl = request.musicUrl
            )
        )
    }
}
