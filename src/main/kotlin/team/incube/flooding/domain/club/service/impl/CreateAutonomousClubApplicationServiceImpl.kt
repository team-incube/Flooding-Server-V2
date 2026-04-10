package team.incube.flooding.domain.club.service.impl

import org.redisson.api.RedissonClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubAutonomousApplicationJpaEntity
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.response.CreateAutonomousClubApplicationResponse
import team.incube.flooding.domain.club.repository.ClubAutonomousApplicationRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.CreateAutonomousClubApplicationService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.concurrent.TimeUnit

@Service
@Transactional
class CreateAutonomousClubApplicationServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubAutonomousApplicationRepository: ClubAutonomousApplicationRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val redissonClient: RedissonClient,
) : CreateAutonomousClubApplicationService {
    override fun execute(clubId: Long): CreateAutonomousClubApplicationResponse {
        val user = currentUserProvider.getCurrentUser()

        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }

        if (club.type != ClubType.AUTONOMOUS_CLUB) {
            throw ExpectedException("자율 동아리만 선착순 신청이 가능합니다.", HttpStatus.BAD_REQUEST)
        }

        val maxMember =
            club.maxMember
                ?: throw ExpectedException("정원이 설정되지 않은 동아리입니다.", HttpStatus.BAD_REQUEST)

        val lock = redissonClient.getLock("autonomous-club-application:$clubId")
        val acquired = lock.tryLock(5, 3, TimeUnit.SECONDS)

        if (!acquired) {
            throw ExpectedException("잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS)
        }

        try {
            if (clubAutonomousApplicationRepository.existsByClubIdAndUserId(clubId, user.id)) {
                throw ExpectedException("이미 신청한 동아리입니다.", HttpStatus.CONFLICT)
            }

            if (clubAutonomousApplicationRepository.countByClubId(clubId) >= maxMember) {
                throw ExpectedException("신청 정원이 마감되었습니다.", HttpStatus.CONFLICT)
            }

            val application =
                clubAutonomousApplicationRepository.save(
                    ClubAutonomousApplicationJpaEntity(club = club, user = user),
                )

            return CreateAutonomousClubApplicationResponse(applicationId = application.id)
        } finally {
            lock.unlock()
        }
    }
}
