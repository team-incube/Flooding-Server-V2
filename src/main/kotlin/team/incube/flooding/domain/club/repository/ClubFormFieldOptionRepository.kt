package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity

interface ClubFormFieldOptionRepository : JpaRepository<ClubFormFieldOptionJpaEntity, Long> {
    fun findAllByFieldIdInOrderByOptionOrder(fieldIds: List<Long>): List<ClubFormFieldOptionJpaEntity>

    @Modifying
    @Query("DELETE FROM ClubFormFieldOptionJpaEntity o WHERE o.field.id IN :fieldIds")
    fun deleteAllByFieldIdIn(fieldIds: List<Long>)
}
