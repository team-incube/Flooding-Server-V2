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

    @field:Column(name = "table_number", nullable = false)
    val tableNumber: Int,

    @field:Column(name = "is_attended", nullable = false)
    val isAttended: Boolean = false
)