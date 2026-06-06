package team.incube.flooding.domain.user.service

import team.incube.flooding.domain.user.entity.Role

interface PatchUserRoleService {
    fun execute(
        userId: Long,
        role: Role,
    )
}
