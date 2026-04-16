package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity

interface ClubFormSubmissionRepository : JpaRepository<ClubFormSubmissionJpaEntity, Long> {
    fun existsByFormIdAndUserId(
        formId: Long,
        userId: Long,
    ): Boolean

    fun findAllByFormClubId(clubId: Long): List<ClubFormSubmissionJpaEntity>
}
