package team.incube.flooding.domain.club.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.club.service.ClubApplicationService

@RestController
@RequestMapping("/club")
class ClubApplicationController(
    private val clubApplicationService: ClubApplicationService
) {
    @PatchMapping("/{clubId}/applications/{userId}")
    fun approveApplication(
        @PathVariable clubId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        clubApplicationService.approveApplication(clubId, userId)
        return ResponseEntity.noContent().build()
    }
}