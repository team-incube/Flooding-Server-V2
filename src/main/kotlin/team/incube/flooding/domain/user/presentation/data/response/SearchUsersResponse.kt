package team.incube.flooding.domain.user.presentation.data.response

import team.incube.flooding.domain.user.entity.UserJpaEntity

data class SearchUsersResponse(
    val id: Long,
    val name: String,
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
                studentNumber = user.studentNumber,
                grade = user.grade,
                classNumber = user.classNumber,
                number = user.number,
            )
    }
}
