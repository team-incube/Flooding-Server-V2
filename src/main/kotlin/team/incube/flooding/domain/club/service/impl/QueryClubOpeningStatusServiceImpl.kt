package team.incube.flooding.domain.club.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.club.presentation.data.response.GetClubOpeningStatusResponse
import team.incube.flooding.domain.club.repository.ClubOpeningPeriodRepository
import team.incube.flooding.domain.club.service.QueryClubOpeningStatusService

@Service
class QueryClubOpeningStatusServiceImpl(
    private val clubOpeningPeriodRepository: ClubOpeningPeriodRepository,
) : QueryClubOpeningStatusService {
    override fun execute(): GetClubOpeningStatusResponse {
        val period = clubOpeningPeriodRepository.findFirstByOrderByIdDesc()
        return GetClubOpeningStatusResponse(
            isOpened = period?.isOpened() ?: false,
            startDate = period?.startDate,
            endDate = period?.endDate,
        )
    }
}
