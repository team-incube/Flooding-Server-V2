package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubAutonomousApplicationJpaEntity

interface ClubAutonomousApplicationRepository : JpaRepository<ClubAutonomousApplicationJpaEntity, Long> {
    fun existsByClubIdAndUserId(
        clubId: Long,
        userId: Long,
    ): Boolean

    fun countByClubId(clubId: Long): Long
}
