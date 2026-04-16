package team.incube.flooding.domain.dormitory.cleaningzone.service

import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.AssignCleaningZoneMembersRequest

interface AssignCleaningZoneMembersService {
    fun execute(
        zoneId: Long,
        request: AssignCleaningZoneMembersRequest,
    )
}
