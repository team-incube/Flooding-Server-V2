package team.incube.flooding.domain.homebase.dto.request

import jakarta.validation.constraints.NotEmpty
import team.incube.flooding.domain.homebase.dto.MemberDto

data class UpdateHomebaseMembersRequest(
    @field:NotEmpty(message = "예약 인원은 최소 1명 이상이어야 합니다.")
    val members: List<MemberDto>
)