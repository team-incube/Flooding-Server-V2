package team.incube.flooding.domain.user.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.UserJpaEntity

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserJpaEntity u SET u.cleaningZone = null WHERE u.cleaningZone.id = :zoneId")
    fun clearCleaningZoneByZoneId(zoneId: Long)

    @Query(
        value = """
        SELECT u FROM UserJpaEntity u
        WHERE u.role <> :excludedRole
        AND u.name LIKE :nameLike
        AND (:studentNumberStart IS NULL OR u.studentNumber BETWEEN :studentNumberStart AND :studentNumberEnd)
        """,
        countQuery = """
        SELECT count(u) FROM UserJpaEntity u
        WHERE u.role <> :excludedRole
        AND u.name LIKE :nameLike
        AND (:studentNumberStart IS NULL OR u.studentNumber BETWEEN :studentNumberStart AND :studentNumberEnd)
        """,
    )
    fun searchUsers(
        nameLike: String,
        studentNumberStart: Int?,
        studentNumberEnd: Int?,
        excludedRole: Role,
        pageable: Pageable,
    ): Page<UserJpaEntity>
}
