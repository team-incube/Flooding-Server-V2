package team.incube.flooding.domain.dormitory.cleaningzone.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.dormitory.cleaningzone.entity.CleaningZoneJpaEntity

interface CleaningZoneRepository : JpaRepository<CleaningZoneJpaEntity, Long> {
    @Query("SELECT DISTINCT z FROM CleaningZoneJpaEntity z LEFT JOIN FETCH z.members")
    fun findAllWithMembers(): List<CleaningZoneJpaEntity>
}
