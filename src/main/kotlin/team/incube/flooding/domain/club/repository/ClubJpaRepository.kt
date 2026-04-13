package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus

@Repository
interface ClubJpaRepository : JpaRepository<ClubJpaEntity, Long> {
    fun findAllByStatus(status: ClubStatus): List<ClubJpaEntity>
}
