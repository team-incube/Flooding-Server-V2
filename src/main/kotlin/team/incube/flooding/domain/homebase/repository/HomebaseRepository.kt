package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.homebase.entity.HomebaseJpaEntity

interface HomebaseRepository : JpaRepository<HomebaseJpaEntity, Long>