package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.homebase.entity.HomebaseJpaEntity

interface HomebaseRepository : JpaRepository<HomebaseJpaEntity, Long> {

    fun existsByFloorAndTableNumberAndPeriod(
        floor: Int,
        tableNumber: Int,
        period: Int
    ): Boolean

    fun findAllByPeriod(period: Int): List<HomebaseJpaEntity>
}