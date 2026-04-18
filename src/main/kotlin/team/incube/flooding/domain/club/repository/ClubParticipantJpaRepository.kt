package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.entity.ClubType

interface ClubParticipantJpaRepository : JpaRepository<ClubParticipantJpaEntity, ClubParticipantId> {
    @Query("SELECT cp FROM ClubParticipantJpaEntity cp WHERE cp.club.type = :clubType")
    fun findAllByClubType(
        @Param("clubType") clubType: ClubType,
    ): List<ClubParticipantJpaEntity>
}
