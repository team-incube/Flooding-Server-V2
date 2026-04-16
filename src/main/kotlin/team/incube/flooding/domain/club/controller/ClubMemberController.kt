package team.incube.flooding.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.service.ClubMemberService

@Tag(name = "동아리 멤버", description = "동아리 멤버 관련 API")
@RestController
@RequestMapping("/clubs")
class ClubMemberController(
    private val clubMemberService: ClubMemberService,
) {
    @Operation(summary = "멤버 초대", description = "동아리에 새로운 사용자를 멤버로 초대합니다")
    @PostMapping("/{clubId}/member/{userId}")
    fun execute(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): ResponseEntity<Unit> {
        clubMemberService.inviteMember(clubId, userId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "소유권 위임", description = "동아리 방장이 다른 멤버에게 방장 권한을 넘기고 본인의 역할을 변경합니다")
    @PatchMapping("/{clubId}/transfer/{targetUserId}")
    fun execute(
        @PathVariable clubId: Long,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<Void> {
        clubMemberService.transferOwnerShip(clubId, targetUserId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "구성원 추방하기", description = "관리자 권한으로 특정 멤버를 동아리에서 추방합니다 ")
    @DeleteMapping("/{clubId}/member/exile/{userId}")
    fun execute(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): ResponseEntity<Unit> {
        clubMemberService.exileMember(clubId, userId)
        return ResponseEntity.noContent().build()
    }
}
