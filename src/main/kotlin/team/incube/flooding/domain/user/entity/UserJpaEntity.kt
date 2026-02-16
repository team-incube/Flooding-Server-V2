package team.incube.flooding.domain.user.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class UserJpaEntity(
    @field:Id
    val id: Long,
    var name: String,
    var sex: Sex,
    var email: String,
    var studentNumber: StudentNumber,
    var role: Role,
    var dormitoryRoom: DormitoryRoom
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