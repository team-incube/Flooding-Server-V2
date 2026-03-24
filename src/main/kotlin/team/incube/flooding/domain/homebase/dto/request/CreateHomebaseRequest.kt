package team.incube.flooding.domain.homebase.dto.request

import team.incube.flooding.domain.homebase.dto.MemberDto
import jakarta.validation.constraints.NotEmpty

data class CreateHomebaseRequest(
    val startPeriod: Int,
    val endPeriod: Int,

    @field:NotEmpty(message = "예약 인원은 최소 1명 이상이어야 합니다.")
    val members: List<MemberDto>
)