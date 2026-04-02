package team.incube.flooding.domain.club.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.club.service.ClubMemberService

@RestController
@RequestMapping("/club")
class ClubMemberController(
    private val clubMemberService: ClubMemberService,
) {
    @PostMapping("/{clubId}/member/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun inviteMember(
        @PathVariable clubId: Long,
        @PathVariable userId: Long,
    ) {
        clubMemberService.inviteMember(clubId, userId)
    }
}
