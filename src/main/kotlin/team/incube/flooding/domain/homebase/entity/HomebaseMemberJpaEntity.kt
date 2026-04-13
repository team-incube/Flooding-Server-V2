package team.incube.flooding.domain.homebase.entity

import jakarta.persistence.*

@Entity
@Table(name = "tb_homebase_member")
class HomebaseMemberJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:Column(name = "student_number", nullable = false)
    val studentNumber: String,
    @field:Column(nullable = false)
    val name: String,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "reservation_id", nullable = false)
    @field:com.fasterxml.jackson.annotation.JsonIgnore
    val reservation: HomebaseReservationJpaEntity,
)
