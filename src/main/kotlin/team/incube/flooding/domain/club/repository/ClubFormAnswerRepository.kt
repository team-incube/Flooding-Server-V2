package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity

interface ClubFormAnswerRepository : JpaRepository<ClubFormAnswerJpaEntity, Long> {
    fun findAllBySubmissionIn(submissions: List<ClubFormSubmissionJpaEntity>): List<ClubFormAnswerJpaEntity>
}
