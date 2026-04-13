package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity

interface ClubFormAnswerRepository : JpaRepository<ClubFormAnswerJpaEntity, Long>
