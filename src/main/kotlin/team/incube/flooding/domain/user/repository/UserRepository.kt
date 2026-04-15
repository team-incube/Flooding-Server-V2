package team.incube.flooding.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.user.entity.UserJpaEntity

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserJpaEntity u SET u.cleaningZone = null WHERE u.cleaningZone.id = :zoneId")
    fun clearCleaningZoneByZoneId(zoneId: Long)
}
