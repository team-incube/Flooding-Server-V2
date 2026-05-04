package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import team.incube.flooding.domain.club.entity.ClubOpeningPeriodJpaEntity
import team.incube.flooding.domain.club.presentation.data.request.UpdateClubOpeningPeriodRequest
import team.incube.flooding.domain.club.repository.ClubOpeningPeriodRepository
import team.incube.flooding.domain.club.service.UpdateClubOpeningPeriodService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class UpdateClubOpeningPeriodServiceImpl(
    private val clubOpeningPeriodRepository: ClubOpeningPeriodRepository,
    private val transactionTemplate: TransactionTemplate,
    private val currentUserProvider: CurrentUserProvider,
) : UpdateClubOpeningPeriodService {
    override fun execute(request: UpdateClubOpeningPeriodRequest) {
        val user = currentUserProvider.getCurrentUser()
        if (user.role != Role.ADMIN) throw ExpectedException("권한이 없습니다.", HttpStatus.FORBIDDEN)

        if (request.startDate.isAfter(request.endDate)) {
            throw ExpectedException("시작 시간이 종료 시간보다 늦을 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        transactionTemplate.execute {
            val period = clubOpeningPeriodRepository.findFirstByOrderByIdDesc()
            if (period == null) {
                clubOpeningPeriodRepository.save(
                    ClubOpeningPeriodJpaEntity(startDate = request.startDate, endDate = request.endDate),
                )
            } else {
                period.updatePeriod(request.startDate, request.endDate)
            }
        }
    }
}
