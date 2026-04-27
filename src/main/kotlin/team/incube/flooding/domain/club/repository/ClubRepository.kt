package team.incube.flooding.domain.club.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubType

interface ClubRepository : JpaRepository<ClubJpaEntity, Long> {
    @Query("SELECT c FROM ClubJpaEntity c LEFT JOIN FETCH c.leader WHERE c.id = :id")
    fun findByIdWithLeader(id: Long): ClubJpaEntity?

    fun findAllByType(type: ClubType): List<ClubJpaEntity>

    @Query(
        """
        SELECT c FROM ClubJpaEntity c
        LEFT JOIN c.leader l
        WHERE c.type = :type
        AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """,
    )
    fun findAllByTypeAndKeyword(
        type: ClubType,
        keyword: String,
    ): List<ClubJpaEntity>

    fun findAllByDataGsmClubIdIsNotNull(): List<ClubJpaEntity>
}
