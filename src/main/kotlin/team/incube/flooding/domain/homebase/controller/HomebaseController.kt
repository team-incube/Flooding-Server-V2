package team.incube.flooding.domain.homebase.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.dto.request.UpdateHomebaseMembersRequest
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.service.*

@RestController
@RequestMapping("/homebase")
class HomebaseController(
    private val createService: CreateHomebaseReservationService,
    private val patchService: PatchHomebaseReservationService,
    private val deleteService: DeleteHomebaseReservationService,
    private val getService: GetHomebaseReservationService
) {

    @PostMapping("/{homebaseId}")
    fun createReservation(
        @PathVariable homebaseId: Long,
        @RequestBody @Valid request: CreateHomebaseRequest
    ) {
        createService.createReservation(homebaseId, request)
    }

    @GetMapping
    fun getReservation(): List<GetHomebaseResponse> {
        return getService.getReservationList()
    }

    @DeleteMapping("/{reservationId}")
    fun deleteReservation(
        @PathVariable reservationId: Long,
    ) {
        deleteService.deleteReservation(reservationId)
    }

    @PatchMapping("/{reservationId}")
    fun updateReservation(
        @PathVariable reservationId: Long,
        @RequestBody @Valid request: UpdateHomebaseMembersRequest
    ) {
        patchService.patchReservation(reservationId, request)
    }
}