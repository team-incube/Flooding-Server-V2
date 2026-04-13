package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity

interface ClubFormFieldOptionRepository : JpaRepository<ClubFormFieldOptionJpaEntity, Long> {
    fun findAllByFieldIdInOrderByOptionOrder(fieldIds: List<Long>): List<ClubFormFieldOptionJpaEntity>
}
