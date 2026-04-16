package team.incube.flooding.domain.club.presentation.data.response

import team.incube.flooding.domain.club.entity.ClubApprovalStatus

data class PatchClubApprovalResponse(
    val clubId: Long,
    val status: ClubApprovalStatus,
)
