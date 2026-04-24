package team.incube.flooding.domain.club.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubApprovalStatus
import team.incube.flooding.domain.club.event.ClubApprovedEvent
import team.incube.flooding.domain.club.presentation.data.request.PatchClubApprovalRequest
import team.incube.flooding.domain.club.presentation.data.response.PatchClubApprovalResponse
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.PatchClubApprovalService
import team.themoment.sdk.exception.ExpectedException

@Service
class PatchClubApprovalServiceImpl(
    private val clubRepository: ClubRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PatchClubApprovalService {
    @Transactional
    override fun execute(
        clubId: Long,
        request: PatchClubApprovalRequest,
    ): PatchClubApprovalResponse {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }

        if (club.approvalStatus != ClubApprovalStatus.PENDING) {
            throw ExpectedException("승인 대기 중인 동아리가 아닙니다.", HttpStatus.BAD_REQUEST)
        }

        val newApprovalStatus = if (request.approved) ClubApprovalStatus.APPROVED else ClubApprovalStatus.REJECTED
        club.approvalStatus = newApprovalStatus

        if (newApprovalStatus == ClubApprovalStatus.APPROVED) {
            eventPublisher.publishEvent(ClubApprovedEvent(clubId))
        }

        return PatchClubApprovalResponse(clubId = clubId, status = newApprovalStatus)
    }
}
