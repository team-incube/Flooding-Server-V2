package team.incube.flooding.domain.homebase.controller

import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.service.HomebaseService

@RestController
@RequestMapping("/homebase")
class HomebaseController(

    private val homebaseService: HomebaseService

) {

    @PostMapping("/{homebaseId}")
    fun createReservation(
        @PathVariable homebaseId: Long,
        @RequestBody request: CreateHomebaseRequest
    ) {
        homebaseService.createReservation(homebaseId, request)
    }

    @GetMapping
    fun getReservation(): List<GetHomebaseResponse> {
        return homebaseService.getReservationList()
    }

    @DeleteMapping("/{reservationId}")
    fun deleteReservation(
        @PathVariable reservationId: Long,
    ) {
        homebaseService.deleteReservation(reservationId)
    }

    @PatchMapping("/{reservationId}")
    fun updateReservation(
        @PathVariable reservationId: Long,
        @RequestBody request: CreateHomebaseRequest
    ) {
        homebaseService.updateMembers(reservationId, request)
    }
}