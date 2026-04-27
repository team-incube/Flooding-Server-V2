package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.homebase.entity.HomebaseMemberJpaEntity
import java.time.LocalDate

interface HomebaseMemberRepository : JpaRepository<HomebaseMemberJpaEntity, Long> {
    fun findByReservationId(reservationId: Long): List<HomebaseMemberJpaEntity>

    fun deleteByReservationId(reservationId: Long)

    @Query(
        """
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM HomebaseMemberJpaEntity m
        WHERE m.studentNumber = :studentNumber
        AND m.reservationDate = :reservationDate
        AND m.reservation.startPeriod <= :endPeriod
        AND m.reservation.endPeriod >= :startPeriod
        """,
    )
    fun existsStudentReservationOverlap(
        @Param("studentNumber") studentNumber: String,
        @Param("reservationDate") reservationDate: LocalDate,
        @Param("startPeriod") startPeriod: Int,
        @Param("endPeriod") endPeriod: Int,
    ): Boolean

    @Query(
        """
    SELECT m.studentNumber 
    FROM HomebaseMemberJpaEntity m 
    WHERE m.studentNumber IN :studentNumbers 
    AND m.reservationDate = :reservationDate
    AND m.reservation.startPeriod = :startPeriod
    AND m.reservation.endPeriod = :endPeriod
    AND (:reservationId IS NULL OR m.reservation.id != :reservationId)
""",
    )
    fun findExistingStudentNumbersInPeriod(
        @Param("studentNumbers") studentNumbers: List<String>,
        @Param("reservationDate") reservationDate: LocalDate,
        @Param("startPeriod") startPeriod: Int,
        @Param("endPeriod") endPeriod: Int,
        @Param("reservationId") reservationId: Long?,
    ): List<String>
}
