package team.incube.flooding.domain.dormitory.cleaningzone.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.cleaningzone.entity.CleaningZoneJpaEntity
import team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request.CreateCleaningZoneRequest
import team.incube.flooding.domain.dormitory.cleaningzone.repository.CleaningZoneRepository
import team.incube.flooding.domain.dormitory.cleaningzone.service.CreateCleaningZoneService

@Service
class CreateCleaningZoneServiceImpl(
    private val cleaningZoneRepository: CleaningZoneRepository,
) : CreateCleaningZoneService {
    @Transactional
    override fun execute(request: CreateCleaningZoneRequest): Long =
        cleaningZoneRepository
            .save(
                CleaningZoneJpaEntity(
                    name = request.name,
                    description = request.description,
                ),
            ).id
}
