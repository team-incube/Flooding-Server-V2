package team.incube.flooding.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.service.ClubMemberService

@Tag(name = "동아리 멤버", description = "동아리 멤버 초대 및 권한 위임 등 관리 API")
@RestController
@RequestMapping("/clubs")
class ClubMemberController(
    private val clubMemberService: ClubMemberService,
) {

    @Operation(summary = "멤버 초대", description = "동아리에 새로운 사용자를 멤버로 초대합니다")
    @PostMapping("/{clubId}/member/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun inviteMember(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ) {
        clubMemberService.inviteMember(clubId, userId)
    }
}
