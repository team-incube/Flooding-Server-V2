package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity

interface ClubFormSubmissionRepository : JpaRepository<ClubFormSubmissionJpaEntity, Long> {
    fun existsByFormId(formId: Long): Boolean

    fun existsByUserId(userId: Long): Boolean

    fun existsByFormIdAndUserId(
        formId: Long,
        userId: Long,
    ): Boolean

    @Query(
        """
        SELECT s FROM ClubFormSubmissionJpaEntity s
        JOIN FETCH s.user
        WHERE s.form.id = :formId
        AND NOT EXISTS (
            SELECT p FROM ClubParticipantJpaEntity p
            WHERE p.club.id = s.form.club.id
            AND p.user.id = s.user.id
        )
        ORDER BY s.submittedAt DESC
        """,
    )
    fun findAllByFormIdWithUser(formId: Long): List<ClubFormSubmissionJpaEntity>

    fun findByFormIdAndUserId(
        formId: Long,
        userId: Long,
    ): ClubFormSubmissionJpaEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        DELETE FROM ClubFormSubmissionJpaEntity s
        WHERE s.form.id IN (
            SELECT form.id FROM ClubFormJpaEntity form
            WHERE form.club.id = :clubId
        )
        """,
    )
    fun deleteAllByClubId(clubId: Long)
}
