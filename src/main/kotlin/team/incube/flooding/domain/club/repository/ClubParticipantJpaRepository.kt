package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity

interface ClubParticipantJpaRepository : JpaRepository<ClubParticipantJpaEntity, ClubParticipantId> {
}
