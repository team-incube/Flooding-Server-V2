package team.incube.flooding.domain.club.entity

import jakarta.persistence.*

@Entity
@Table(name = "tb_club_room")
class ClubRoomJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "teacher_name", nullable = false)
    val teacherName: String,
)
