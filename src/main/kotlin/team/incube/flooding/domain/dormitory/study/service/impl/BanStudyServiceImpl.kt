package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.BanStudyService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

@Service
@Transactional
class BanStudyServiceImpl(
    private val userRepository: UserRepository,
    private val studyBanJpaRepository: StudyBanJpaRepository
) : BanStudyService {

    override fun execute(targetUserId: Long) {

        val targetUser = userRepository.findById(targetUserId).orElseThrow {
            ExpectedException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND)
        }

        val now = LocalDateTime.now()
        if(studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(targetUserId, now)) {
            throw ExpectedException("이미 자습 금지 상태입니다.", HttpStatus.CONFLICT)
        }

        studyBanJpaRepository.save(
            StudyBanJpaEntity(
                user = targetUser,
                bannedAt = now,
                bannedUntil = now.plusWeeks(1)
            )
        )
    }
}