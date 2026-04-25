package team.incube.flooding.domain.user.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.user.entity.UserJpaEntity

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserJpaEntity u SET u.cleaningZone = null WHERE u.cleaningZone.id = :zoneId")
    fun clearCleaningZoneByZoneId(zoneId: Long)

    @Query(
        """
        SELECT u FROM UserJpaEntity u
        WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:studentNumber IS NULL OR CAST(u.studentNumber AS string) LIKE CONCAT(:studentNumber, '%'))
        """,
    )
    fun searchUsers(
        name: String?,
        studentNumber: String?,
        pageable: Pageable,
    ): Page<UserJpaEntity>
}
