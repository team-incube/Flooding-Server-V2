package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.homebase.entity.HomebaseMemberJpaEntity

interface HomebaseMemberRepository :
    JpaRepository<HomebaseMemberJpaEntity, Long> {

    fun findByReservationId(reservationId: Long): List<HomebaseMemberJpaEntity>

    fun deleteByReservationId(reservationId: Long)

    @Query(
        """
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM HomebaseMemberJpaEntity m
        WHERE m.studentNumber = :studentNumber
        AND m.reservation.startPeriod <= :endPeriod
        AND m.reservation.endPeriod >= :startPeriod
        """
    )
    fun existsStudentReservationOverlap(
        @Param("studentNumber") studentNumber: String,
        @Param("startPeriod") startPeriod: Int,
        @Param("endPeriod") endPeriod: Int
    ): Boolean
}