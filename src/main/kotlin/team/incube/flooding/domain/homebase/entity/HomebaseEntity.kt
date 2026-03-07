package team.incube.flooding.domain.homebase.entity
import jakarta.persistence.*

@Entity
@Table(name = "tb_homebase")
class HomebaseJpaEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Column(name = "period", nullable = false)
    val period: Int,

    @field:Column(name = "floor", nullable = false)
    val floor: Int,

    @field:Column(name = "table_id", nullable = false)
    val tableId: Long,

    @field:Column(name = "reason")
    val reason: String? = null
)