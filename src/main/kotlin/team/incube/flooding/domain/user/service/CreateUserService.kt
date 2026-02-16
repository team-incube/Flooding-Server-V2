package team.incube.flooding.domain.user.service

import org.springframework.stereotype.Service
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.datagsm.sdk.oauth.model.StudentRole
import team.themoment.datagsm.sdk.oauth.model.UserInfo

@Service
class CreateUserService(
    private val userRepository: UserRepository
) {
    fun execute(oauthUser: UserInfo) {
        if (!oauthUser.isStudent()) throw IllegalArgumentException("학생이 아닙니다.")

        val student = oauthUser.student ?: throw IllegalArgumentException("학생 정보가 없습니다.")

        val user = UserJpaEntity(
            id = student.id,
            name = student.name,
            sex = when (student.sex) {
                team.themoment.datagsm.sdk.oauth.model.Sex.MAN -> Sex.MAN
                team.themoment.datagsm.sdk.oauth.model.Sex.WOMAN -> Sex.WOMAN
            },
            email = student.email,
            studentNumber = UserJpaEntity.StudentNumber(student.studentNumber),
            role = when (student.role) {
                StudentRole.GENERAL_STUDENT -> Role.GENERAL_STUDENT
                StudentRole.STUDENT_COUNCIL -> Role.STUDENT_COUNCIL
                StudentRole.DORMITORY_MANAGER -> Role.DORMITORY_MANAGER
            },
            dormitoryRoom = UserJpaEntity.DormitoryRoom(student.dormitoryRoom ?: 0)
        )

        userRepository.save(user)
    }
}