package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity

interface ClubFormAnswerRepository : JpaRepository<ClubFormAnswerJpaEntity, Long> {
    @Query(
        "SELECT a FROM ClubFormAnswerJpaEntity a JOIN FETCH a.field WHERE a.submission.id IN :submissionIds ORDER BY a.field.fieldOrder ASC",
    )
    fun findAllBySubmissionIdIn(submissionIds: List<Long>): List<ClubFormAnswerJpaEntity>

    @Modifying
    @Query("DELETE FROM ClubFormAnswerJpaEntity a WHERE a.submission.id = :submissionId")
    fun deleteAllBySubmissionId(submissionId: Long)

    @Modifying
    @Query(
        """
        DELETE FROM ClubFormAnswerJpaEntity a
        WHERE a.submission.id IN (
            SELECT s.id FROM ClubFormSubmissionJpaEntity s
            WHERE s.form.club.id = :clubId
        )
        """,
    )
    fun deleteAllByClubId(clubId: Long)
}
