package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity

interface ClubFormRepository : JpaRepository<ClubFormJpaEntity, Long> {
    fun findByClubIdAndIsActiveTrue(clubId: Long): ClubFormJpaEntity?
}
