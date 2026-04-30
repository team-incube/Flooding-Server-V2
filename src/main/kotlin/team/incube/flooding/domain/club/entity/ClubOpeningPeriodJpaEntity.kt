package team.incube.flooding.domain.club.entity

import jakarta.persistence.*
import java.time.Clock
import java.time.LocalDateTime

@Entity
@Table(name = "club_opening_period")
class ClubOpeningPeriodJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var startDate: LocalDateTime,
    @Column(nullable = false)
    var endDate: LocalDateTime,
) {
    fun updatePeriod(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ) {
        this.startDate = startDate
        this.endDate = endDate
    }

    fun isOpened(clock: Clock): Boolean {
        val now = LocalDateTime.now(clock)
        return !now.isBefore(startDate) && !now.isAfter(endDate)
    }
}
