package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubOpeningPeriodJpaEntity

interface ClubOpeningPeriodRepository : JpaRepository<ClubOpeningPeriodJpaEntity, Long> {
    fun findFirstByOrderByIdDesc(): ClubOpeningPeriodJpaEntity?
}
