package team.incube.flooding.domain.user.presentation.data.request

import jakarta.validation.constraints.NotNull
import team.incube.flooding.domain.user.entity.Role

data class PatchUserRoleRequest(
    @field:NotNull
    val role: Role,
)
