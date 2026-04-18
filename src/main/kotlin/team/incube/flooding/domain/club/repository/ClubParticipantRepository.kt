package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity

interface ClubMemberCountProjection {
    val clubId: Long
    val count: Long
}

interface ClubParticipantRepository : JpaRepository<ClubParticipantJpaEntity, ClubParticipantId> {
    @Query(
        "SELECT p.club.id as clubId, COUNT(p) as count FROM ClubParticipantJpaEntity p WHERE p.club.id IN :clubIds GROUP BY p.club.id",
    )
    fun countGroupByClubIdIn(clubIds: List<Long>): List<ClubMemberCountProjection>

    @Query("SELECT p FROM ClubParticipantJpaEntity p JOIN FETCH p.user WHERE p.club.id = :clubId")
    fun findAllByClubId(clubId: Long): List<ClubParticipantJpaEntity>
}
