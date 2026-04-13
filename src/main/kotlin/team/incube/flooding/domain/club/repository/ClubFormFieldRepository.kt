package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity

interface ClubFormFieldRepository : JpaRepository<ClubFormFieldJpaEntity, Long> {
    fun findAllByFormIdOrderByFieldOrder(formId: Long): List<ClubFormFieldJpaEntity>
}
