package team.incube.flooding.domain.dormitory.cleaningzone.service

import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.CreateCleaningZoneRequest

interface CreateCleaningZoneService {
    fun execute(request: CreateCleaningZoneRequest): Long
}
