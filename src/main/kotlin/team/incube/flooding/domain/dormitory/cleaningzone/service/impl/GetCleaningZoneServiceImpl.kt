package team.incube.flooding.domain.dormitory.cleaningzone.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.CleaningZoneMemberDto
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneDetailResponse
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.response.GetCleaningZoneResponse
import team.incube.flooding.domain.dormitory.cleaningzone.repository.CleaningZoneRepository
import team.incube.flooding.domain.dormitory.cleaningzone.service.GetCleaningZoneService
import team.themoment.sdk.exception.ExpectedException

@Service
class GetCleaningZoneServiceImpl(
    private val cleaningZoneRepository: CleaningZoneRepository,
) : GetCleaningZoneService {
    @Transactional(readOnly = true)
    override fun executeList(): List<GetCleaningZoneResponse> =
        cleaningZoneRepository.findAllWithMembers().map { zone ->
            GetCleaningZoneResponse(
                id = zone.id,
                name = zone.name,
                description = zone.description,
                memberCount = zone.members.size,
            )
        }

    @Transactional(readOnly = true)
    override fun executeOne(zoneId: Long): GetCleaningZoneDetailResponse {
        val zone =
            cleaningZoneRepository.findById(zoneId).orElseThrow {
                ExpectedException("존재하지 않는 청소 구역입니다.", HttpStatus.NOT_FOUND)
            }
        return GetCleaningZoneDetailResponse(
            id = zone.id,
            name = zone.name,
            description = zone.description,
            members =
                zone.members.map { user ->
                    CleaningZoneMemberDto(
                        userId = user.id,
                        name = user.name,
                        studentNumber = user.studentNumber,
                    )
                },
        )
    }
}
