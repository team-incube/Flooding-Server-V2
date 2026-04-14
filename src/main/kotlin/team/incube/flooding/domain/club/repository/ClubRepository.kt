package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.club.entity.ClubApprovalStatus
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubType

interface ClubRepository : JpaRepository<ClubJpaEntity, Long> {
    fun findAllByType(type: ClubType): List<ClubJpaEntity>

    @Query(
        """
        SELECT c FROM ClubJpaEntity c
        LEFT JOIN c.leader l
        WHERE c.type = :type
        AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """,
    )
    fun findAllByTypeAndKeyword(
        type: ClubType,
        keyword: String,
    ): List<ClubJpaEntity>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ClubJpaEntity c SET c.approvalStatus = :approvalStatus WHERE c.id = :clubId")
    fun updateApprovalStatus(
        @Param("clubId") clubId: Long,
        @Param("approvalStatus") approvalStatus: ClubApprovalStatus,
    )
}
