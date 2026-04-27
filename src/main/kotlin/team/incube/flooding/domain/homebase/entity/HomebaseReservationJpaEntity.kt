package team.incube.flooding.domain.homebase.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import team.incube.flooding.domain.homebase.dto.MemberDto
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import java.time.LocalDate

@Entity
@Table(name = "tb_homebase_reservation")
class HomebaseReservationJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val reservationDate: LocalDate,
    @field:Column(nullable = false)
    val startPeriod: Int,
    @field:Column(nullable = false)
    val endPeriod: Int,
    @field:Column(nullable = false, length = 300)
    val reason: String = "",
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "homebase_id", nullable = false)
    val homebase: HomebaseJpaEntity,
    @field:OneToMany(mappedBy = "reservation")
    val members: List<HomebaseMemberJpaEntity> = mutableListOf(),
) {
    fun toResponse() =
        GetHomebaseResponse(
            id = id,
            reservationDate = reservationDate,
            startPeriod = startPeriod,
            endPeriod = endPeriod,
            reason = reason,
            homebaseId = homebase.id,
            members = members.map { MemberDto(it.studentNumber, it.name) },
        )
}
