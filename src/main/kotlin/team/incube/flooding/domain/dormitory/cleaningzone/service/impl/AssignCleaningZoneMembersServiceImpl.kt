package team.incube.flooding.domain.dormitory.cleaningzone.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.AssignCleaningZoneMembersRequest
import team.incube.flooding.domain.dormitory.cleaningzone.repository.CleaningZoneRepository
import team.incube.flooding.domain.dormitory.cleaningzone.service.AssignCleaningZoneMembersService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException

@Service
class AssignCleaningZoneMembersServiceImpl(
    private val cleaningZoneRepository: CleaningZoneRepository,
    private val userRepository: UserRepository,
) : AssignCleaningZoneMembersService {
    @Transactional
    override fun execute(
        zoneId: Long,
        request: AssignCleaningZoneMembersRequest,
    ) {
        val zone =
            cleaningZoneRepository.findById(zoneId).orElseThrow {
                ExpectedException("존재하지 않는 청소 구역입니다.", HttpStatus.NOT_FOUND)
            }

        userRepository.clearCleaningZoneByZoneId(zoneId)

        val newMembers = userRepository.findAllById(request.userIds)
        if (newMembers.size != request.userIds.distinct().size) {
            throw ExpectedException("존재하지 않는 유저가 포함되어 있습니다.", HttpStatus.NOT_FOUND)
        }
        newMembers.forEach { it.cleaningZone = zone }
        userRepository.saveAll(newMembers)
    }
}
