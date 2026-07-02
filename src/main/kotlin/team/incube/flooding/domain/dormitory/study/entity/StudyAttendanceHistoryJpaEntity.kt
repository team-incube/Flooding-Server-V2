package team.incube.flooding.domain.dormitory.study.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.time.LocalDate

@Entity
@Table(
    name = "tb_study_attendance_history",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "attended_date"]),
    ],
)
class StudyAttendanceHistoryJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(nullable = false, name = "user_id")
    val user: UserJpaEntity,
    @field:Column(name = "attended_date", nullable = false)
    val attendedDate: LocalDate,
)
