package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import java.time.LocalDate

interface HomebaseReservationRepository : JpaRepository<HomebaseReservationJpaEntity, Long> {
    @Query(
        """
        SELECT r FROM HomebaseReservationJpaEntity r
        WHERE r.homebase.id = :homebaseId
        AND r.reservationDate = :reservationDate
        AND r.startPeriod <= :endPeriod
        AND r.endPeriod >= :startPeriod
        """,
    )
    fun findOverlappingReservation(
        @Param("homebaseId") homebaseId: Long,
        @Param("reservationDate") reservationDate: LocalDate,
        @Param("startPeriod") startPeriod: Int,
        @Param("endPeriod") endPeriod: Int,
    ): List<HomebaseReservationJpaEntity>

    @Query(
        """
        SELECT DISTINCT r 
        FROM HomebaseReservationJpaEntity r 
        JOIN FETCH r.homebase 
        LEFT JOIN FETCH r.members
    """,
    )
    fun findAllWithMembers(): List<HomebaseReservationJpaEntity>
}
