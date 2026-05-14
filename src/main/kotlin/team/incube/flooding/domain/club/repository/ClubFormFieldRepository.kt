package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity

interface ClubFormFieldRepository : JpaRepository<ClubFormFieldJpaEntity, Long> {
    fun findAllByFormIdOrderByFieldOrder(formId: Long): List<ClubFormFieldJpaEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ClubFormFieldJpaEntity f WHERE f.form.id = :formId")
    fun deleteAllByFormId(formId: Long)

    @Modifying
    @Query(
        """
        DELETE FROM ClubFormFieldJpaEntity f
        WHERE f.form.id IN (
            SELECT form.id FROM ClubFormJpaEntity form
            WHERE form.club.id = :clubId
        )
        """,
    )
    fun deleteAllByClubId(clubId: Long)
}
