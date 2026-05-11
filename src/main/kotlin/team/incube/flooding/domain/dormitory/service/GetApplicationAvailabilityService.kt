package team.incube.flooding.domain.dormitory.service

import team.incube.flooding.domain.dormitory.presentation.data.response.GetApplicationAvailabilityResponse

interface GetApplicationAvailabilityService {
    fun execute(): GetApplicationAvailabilityResponse
}
