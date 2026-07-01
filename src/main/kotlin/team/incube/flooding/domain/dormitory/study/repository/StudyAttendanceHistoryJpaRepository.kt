package team.incube.flooding.domain.dormitory.study.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.flooding.domain.dormitory.study.entity.StudyAttendanceHistoryJpaEntity
import java.time.LocalDate

interface StudyAttendanceHistoryJpaRepository : JpaRepository<StudyAttendanceHistoryJpaEntity, Long> {
    fun existsByUserIdAndAttendedDate(
        userId: Long,
        attendedDate: LocalDate,
    ): Boolean

    fun deleteByUserIdAndAttendedDate(
        userId: Long,
        attendedDate: LocalDate,
    )

    @Query(
        "SELECT h FROM StudyAttendanceHistoryJpaEntity h JOIN FETCH h.user " +
            "WHERE h.attendedDate BETWEEN :startDate AND :endDate ORDER BY h.attendedDate ASC",
    )
    fun findAllByAttendedDateBetweenOrderByAttendedDateAsc(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<StudyAttendanceHistoryJpaEntity>
}
