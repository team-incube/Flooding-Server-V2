package team.incube.flooding.domain.homebase.entity

import jakarta.persistence.*

@Entity
@Table(name = "tb_homebase_reservation")
class HomebaseReservationJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val startPeriod: Int,

    val endPeriod: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homebase_id")
    val homebase: HomebaseJpaEntity
)