package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity

interface ClubFormRepository : JpaRepository<ClubFormJpaEntity, Long> {
    fun findByClubIdAndIsActiveTrue(clubId: Long): ClubFormJpaEntity?

    fun findAllByClubIdAndIsActiveTrue(clubId: Long): List<ClubFormJpaEntity>

    @Modifying
    @Query("DELETE FROM ClubFormJpaEntity f WHERE f.club.id = :clubId")
    fun deleteAllByClubId(clubId: Long)
}
