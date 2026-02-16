package team.incube.flooding.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.user.entity.UserJpaEntity

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
}