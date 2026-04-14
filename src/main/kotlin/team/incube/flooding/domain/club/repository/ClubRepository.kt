package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus

interface ClubRepository : JpaRepository<ClubJpaEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ClubJpaEntity c SET c.status = :status WHERE c.id = :clubId")
    fun updateStatus(
        @Param("clubId") clubId: Long,
        @Param("status") status: ClubStatus,
    )
}
