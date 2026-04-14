package team.incube.flooding.domain.club.presentation.data.response

import team.incube.flooding.domain.club.entity.ClubStatus

data class PatchClubApprovalResponse(
    val clubId: Long,
    val status: ClubStatus,
)
