package team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request

import jakarta.validation.constraints.NotNull

data class AssignCleaningZoneMembersRequest(
    @field:NotNull(message = "배정할 유저 ID 목록은 필수입니다.")
    val userIds: List<Long>,
)
