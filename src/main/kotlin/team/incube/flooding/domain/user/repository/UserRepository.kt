package team.incube.flooding.domain.user.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.user.entity.UserJpaEntity

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    fun findAllByOrderByPenaltyScoreDescStudentNumberAsc(pageable: Pageable): Page<UserJpaEntity>
}
