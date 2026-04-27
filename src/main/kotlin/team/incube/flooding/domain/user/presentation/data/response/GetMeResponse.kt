package team.incube.flooding.domain.user.presentation.data.response

import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity

data class GetMeResponse(
    val id: Long,
    val name: String,
    val sex: Sex,
    val email: String,
    val studentNumber: Int,
    val grade: Int,
    val classNumber: Int,
    val number: Int,
    val role: Role,
    val dormitoryRoom: Int,
    val dormitoryFloor: Int,
    val specialty: String?,
    val penaltyScore: Int,
) {
    companion object {
        fun from(user: UserJpaEntity) =
            GetMeResponse(
                id = user.id,
                name = user.name,
                sex = user.sex,
                email = user.email,
                studentNumber = user.studentNumber,
                grade = user.grade,
                classNumber = user.classNumber,
                number = user.number,
                role = user.role,
                dormitoryRoom = user.dormitoryRoom,
                dormitoryFloor = user.dormitoryFloor,
                specialty = user.specialty,
                penaltyScore = user.penaltyScore,
            )
    }
}
