package team.incube.flooding.domain.dormitory.cleaningzone.service

import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneDetailResponse
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneResponse

interface GetCleaningZoneService {
    fun executeList(): List<GetCleaningZoneResponse>

    fun executeOne(zoneId: Long): GetCleaningZoneDetailResponse
}
