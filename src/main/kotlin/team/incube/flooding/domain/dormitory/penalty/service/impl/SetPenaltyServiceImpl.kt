package team.incube.flooding.domain.dormitory.penalty.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.penalty.entity.DormitoryPenaltyHistoryJpaEntity
import team.incube.flooding.domain.dormitory.penalty.presentation.data.request.SetPenaltyRequest
import team.incube.flooding.domain.dormitory.penalty.repository.DormitoryPenaltyHistoryRepository
import team.incube.flooding.domain.dormitory.penalty.service.SetPenaltyService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class SetPenaltyServiceImpl(
    private val userRepository: UserRepository,
    private val penaltyHistoryRepository: DormitoryPenaltyHistoryRepository,
    private val currentUserProvider: CurrentUserProvider,
) : SetPenaltyService {
    @Transactional
    override fun execute(
        userId: Long,
        request: SetPenaltyRequest,
    ) {
        val admin = currentUserProvider.getCurrentUser()
        val user =
            userRepository.findById(userId).orElseThrow {
                ExpectedException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND)
            }
        val previousScore = user.penaltyScore
        user.penaltyScore = request.score
        penaltyHistoryRepository.save(
            DormitoryPenaltyHistoryJpaEntity(
                user = user,
                changedBy = admin,
                previousScore = previousScore,
                newScore = request.score,
                reason = request.reason,
            ),
        )
    }
}
