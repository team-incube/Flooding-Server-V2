package team.incube.flooding.domain.club.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.club.service.ExileMemberService
import team.incube.flooding.domain.club.service.InviteMemberService
import team.incube.flooding.domain.club.service.TransferOwnershipService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "동아리 멤버", description = "동아리 멤버 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubMemberController(
    private val inviteMemberService: InviteMemberService,
    private val transferOwnershipService: TransferOwnershipService,
    private val exileMemberService: ExileMemberService,
) {
    @Operation(summary = "멤버 초대", description = "동아리에 새로운 사용자를 멤버로 초대합니다")
    @PostMapping("/{clubId}/member/{userId}")
    fun inviteMember(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        inviteMemberService.execute(clubId, userId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "소유권 위임", description = "동아리 방장이 다른 멤버에게 방장 권한을 넘기고 본인의 역할을 변경합니다")
    @PatchMapping("/{clubId}/transfer/{targetUserId}")
    fun transferOwnership(
        @PathVariable clubId: Long,
        @PathVariable targetUserId: Long,
    ): CommonApiResponse<Nothing> {
        transferOwnershipService.execute(clubId, targetUserId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "구성원 추방하기", description = "관리자 권한으로 특정 멤버를 동아리에서 추방합니다")
    @DeleteMapping("/{clubId}/member/exile/{userId}")
    fun exileMember(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): CommonApiResponse<Nothing> {
        exileMemberService.execute(clubId, userId)
        return CommonApiResponse.success("OK")
    }
}
