package team.incube.flooding.domain.dormitory.penalty.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.penalty.presentation.data.response.GetPenaltyResponse
import team.incube.flooding.domain.dormitory.penalty.service.GetAllPenaltyService
import team.incube.flooding.domain.user.repository.UserRepository

@Service
class GetAllPenaltyServiceImpl(
    private val userRepository: UserRepository,
) : GetAllPenaltyService {
    @Transactional(readOnly = true)
    override fun execute(pageable: Pageable): Page<GetPenaltyResponse> =
        userRepository
            .findAllByOrderByPenaltyScoreDescStudentNumberAsc(pageable)
            .map { user ->
                GetPenaltyResponse(
                    userId = user.id,
                    name = user.name,
                    studentNumber = user.studentNumber,
                    penaltyScore = user.penaltyScore,
                )
            }
}
