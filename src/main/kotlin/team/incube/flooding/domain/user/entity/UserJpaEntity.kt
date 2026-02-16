package team.incube.flooding.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

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
    @JvmInline
    value class StudentNumber(val value: Int){
        override fun toString(): String {
            return value.toString()
        }
        fun getGrade(): Int {
            return value / 1000
        }
        fun getClassNumber(): Int {
            return value % 1000 / 100
        }
        fun getNumber(): Int {
            return value % 100
        }
    }
    @JvmInline
    value class DormitoryRoom(val value: Int) {
        override fun toString(): String {
            return value.toString()
        }
        fun getFloor(): Int {
            return value / 100
        }
    }
}