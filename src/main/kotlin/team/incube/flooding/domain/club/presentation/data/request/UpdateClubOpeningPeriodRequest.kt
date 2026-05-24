package team.incube.flooding.domain.club.presentation.data.request

import java.time.LocalDateTime

data class UpdateClubOpeningPeriodRequest(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
)
