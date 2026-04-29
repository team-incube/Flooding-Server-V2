package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.dormitory.study.service.UnbanStudyService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException
import java.time.Clock
import java.time.LocalDateTime

@Service
@Transactional
class UnbanStudyServiceImpl(
    private val userRepository: UserRepository,
    private val studyBanJpaRepository: StudyBanJpaRepository,
    private val clock: Clock,
) : UnbanStudyService {
    override fun execute(targetUserId: Long) {
        val studyBan =
            studyBanJpaRepository.findByUserIdAndBannedUntilAfter(
                targetUserId,
                LocalDateTime.now(clock),
            ) ?: if (!userRepository.existsById(targetUserId)) {
                throw ExpectedException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND)
            } else {
                throw ExpectedException("자습 금지 내역이 없습니다.", HttpStatus.NOT_FOUND)
            }

        studyBanJpaRepository.delete(studyBan)
    }
}
