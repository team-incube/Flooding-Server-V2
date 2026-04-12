package team.incube.flooding.domain.homebase.entity

import jakarta.persistence.*

@Entity
@Table(name = "tb_homebase")
class HomebaseJpaEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Column(nullable = false)
    val floor: Int,

    @field:Column(name = "table_number", nullable = false)
    val tableNumber: Int,

    @field:Column(nullable = false)
    val capacity: Int
)