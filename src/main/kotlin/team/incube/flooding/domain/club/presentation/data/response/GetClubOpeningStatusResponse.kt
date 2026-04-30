package team.incube.flooding.domain.club.presentation.data.response

import java.time.LocalDateTime

data class GetClubOpeningStatusResponse(
    val isOpened: Boolean,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
)
