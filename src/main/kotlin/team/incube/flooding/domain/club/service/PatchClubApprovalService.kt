package team.incube.flooding.domain.club.service

import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.presentation.data.response.PatchClubApprovalResponse

interface PatchClubApprovalService {
    fun execute(
        clubId: Long,
        request: PatchClubApprovalRequest,
    ): PatchClubApprovalResponse
}
