package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity

interface ClubFormSubmissionRepository : JpaRepository<ClubFormSubmissionJpaEntity, Long> {
    fun existsByFormIdAndUserId(
        formId: Long,
        userId: Long,
    ): Boolean

    @Query("SELECT s FROM ClubFormSubmissionJpaEntity s JOIN FETCH s.user WHERE s.form.id = :formId")
    fun findAllByFormIdWithUser(formId: Long): List<ClubFormSubmissionJpaEntity>
}
