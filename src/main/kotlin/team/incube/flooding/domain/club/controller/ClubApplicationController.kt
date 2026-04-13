package team.incube.flooding.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.service.ClubApplicationService

@Tag(name = "동아리 신청", description = "동아리 가입 신청 승인 및 거절 등 관리 API")
@RestController
@RequestMapping("/clubs")
class ClubApplicationController(
    private val clubApplicationService: ClubApplicationService,
) {

    @Operation(summary = "가입 신청 승인", description = "동아리에 가입 신청한 사용자를 승인하여 멤버로 추가합니다")
    @PatchMapping("/{clubId}/applications/{userId}")
    fun approveApplication(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ): ResponseEntity<Unit> {
        clubApplicationService.approveApplication(clubId, userId)
        return ResponseEntity.noContent().build()
    }
}
