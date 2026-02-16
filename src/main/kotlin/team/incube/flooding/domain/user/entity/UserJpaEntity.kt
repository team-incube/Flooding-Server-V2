package team.incube.flooding.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient

@Entity
@Table(name = "tb_user")
class UserJpaEntity(
    @field:Id
    @field:Column(name = "id")
    val id: Long,

    @field:Column(name = "name")
    var name: String,

    @field:Column(name = "sex")
    var sex: Sex,

    @field:Column(name = "email")
    var email: String,

    @field:Column(name = "student_number")
    var studentNumber: Int,

    @field:Column(name = "role")
    var role: Role,

    @field:Column(name = "dormitory_room")
    var dormitoryRoom: Int
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