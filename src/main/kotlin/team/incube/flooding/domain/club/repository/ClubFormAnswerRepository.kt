package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity

interface ClubFormAnswerRepository : JpaRepository<ClubFormAnswerJpaEntity, Long> {
    @Query("SELECT a FROM ClubFormAnswerJpaEntity a JOIN FETCH a.field WHERE a.submission.id IN :submissionIds")
    fun findAllBySubmissionIdIn(submissionIds: List<Long>): List<ClubFormAnswerJpaEntity>
}
