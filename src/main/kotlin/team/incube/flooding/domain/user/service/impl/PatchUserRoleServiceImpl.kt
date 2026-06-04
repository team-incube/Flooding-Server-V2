package team.incube.flooding.domain.user.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.PatchUserRoleService
import team.themoment.sdk.exception.ExpectedException

@Service
class PatchUserRoleServiceImpl(
    private val userRepository: UserRepository,
) : PatchUserRoleService {
    @Transactional
    override fun execute(
        userId: Long,
        role: Role,
    ) {
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { ExpectedException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND) }
        user.role = role
        userRepository.save(user)
    }
}
