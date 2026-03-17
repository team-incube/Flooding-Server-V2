package team.incube.flooding.domain.homebase.controller

import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.service.HomebaseService

@RestController
@RequestMapping("/homebase")
class HomebaseController(

    private val homebaseService: HomebaseService

) {

    @PostMapping("/{homebaseId}")
    fun createReservatio(
        @PathVariable homebaseId: Long,
        @RequestBody request: CreateHomebaseRequest
    ) {
        val newRequest = request.copy(homebaseId = homebaseId)
        homebaseService.createReservation(newRequest)
    }

    @GetMapping
    fun getReservation(): List<HomebaseReservationJpaEntity> {
        return homebaseService.getReservationList()
    }

    @DeleteMapping("/{reservationId}")
    fun deleteReservation(
        @PathVariable homebaseId: Long,
    ) {
        homebaseService.deleteReservation(homebaseId)
    }

    @PatchMapping("/{reservationId}")
    fun updateReservation(
        @PathVariable homebaseId: Long,
        @RequestBody request: CreateHomebaseRequest
    ) {
        homebaseService.updateMembers(homebaseId, request)
    }
}