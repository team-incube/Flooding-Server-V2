package team.incube.flooding.domain.homebase.entity

import jakarta.persistence.*

@Entity
@Table(name = "tb_homebase_reservation")
class HomebaseReservationJpaEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Column(nullable = false)
    val startPeriod: Int,

    @field:Column(nullable = false)
    val endPeriod: Int,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "homebase_id", nullable = false)
    val homebase: HomebaseJpaEntity
)