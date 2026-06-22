package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.EntityGraph
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
        SELECT r
        FROM HomebaseReservationJpaEntity r
        JOIN FETCH r.homebase
    """,
    )
    fun findAllWithHomebase(): List<HomebaseReservationJpaEntity>

    @Query(
        """
        SELECT r
        FROM HomebaseReservationJpaEntity r
        JOIN FETCH r.homebase
        WHERE r.reservationDate = :reservationDate
    """,
    )
    fun findAllWithHomebaseByDate(
        @Param("reservationDate")
        reservationDate: LocalDate,
    ): List<HomebaseReservationJpaEntity>

    @EntityGraph(attributePaths = ["homebase"])
    fun findAllByReservationDate(
        reservationDate: LocalDate,
    ): List<HomebaseReservationJpaEntity>
}
