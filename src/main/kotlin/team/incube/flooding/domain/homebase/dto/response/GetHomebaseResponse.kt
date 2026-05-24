package team.incube.flooding.domain.homebase.dto.response

import team.incube.flooding.domain.homebase.dto.MemberDto
import java.time.LocalDate

data class GetHomebaseResponse(
    val id: Long,
    val reservationDate: LocalDate,
    val startPeriod: Int,
    val endPeriod: Int,
    val reason: String,
    val homebaseId: Long,
    val members: List<MemberDto>,
)
