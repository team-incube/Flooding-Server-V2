package team.incube.flooding.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import team.incube.flooding.domain.dormitory.cleaningzone.entity.CleaningZoneJpaEntity

@Entity
@Table(name = "tb_user")
class UserJpaEntity(
    @field:Id
    @field:Column(name = "id")
    val id: Long,
    @field:Column(name = "name", nullable = false)
    var name: String,
    @field:Column(name = "sex", nullable = false)
    @field:Enumerated(EnumType.STRING)
    var sex: Sex,
    @field:Column(name = "email", nullable = false, unique = true)
    var email: String,
    @field:Column(name = "student_number", nullable = false)
    var studentNumber: Int,
    @field:Column(name = "role", nullable = false)
    @field:Enumerated(EnumType.STRING)
    var role: Role,
    @field:Column(name = "dormitory_room", nullable = false)
    var dormitoryRoom: Int,
    @field:Column(name = "specialty", length = 100)
    var specialty: String? = null,
    @field:Column(name = "penalty_score", nullable = false)
    var penaltyScore: Int = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "cleaning_zone_id", nullable = true)
    var cleaningZone: CleaningZoneJpaEntity? = null,
) {
    @get:Transient
    val grade: Int
        get() = studentNumber / 1000

    @get:Transient
    val classNumber: Int
        get() = studentNumber % 1000 / 100

    @get:Transient
    val number: Int
        get() = studentNumber % 100

    @get:Transient
    val dormitoryFloor: Int
        get() = dormitoryRoom / 100
}
