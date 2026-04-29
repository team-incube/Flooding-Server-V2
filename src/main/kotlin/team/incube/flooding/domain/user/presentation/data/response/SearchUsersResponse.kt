package team.incube.flooding.domain.user.presentation.data.response

import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity

data class SearchUsersResponse(
    val id: Long,
    val name: String,
    val sex: Sex,
    val studentNumber: Int,
    val grade: Int,
    val classNumber: Int,
    val number: Int,
) {
    companion object {
        fun from(user: UserJpaEntity) =
            SearchUsersResponse(
                id = user.id,
                name = user.name,
                sex = user.sex,
                studentNumber = user.studentNumber,
                grade = user.grade,
                classNumber = user.classNumber,
                number = user.number,
            )
    }
}
