package team.incube.flooding.domain.homebase.controller

import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest

@RestController
@RequestMapping("/homebase")
class HomebaseController (
    private val homebaseService: HomebaseService
) {
    @PostMapping("/{homebaseId}")
    fun createHomebases(
        @PathVariable homebaseId: Long,
        @RequestBody request: CreateHomebaseRequest
    ) {
        homebaseService.createHomebases(request)
    }

    @GetMapping
    fun getHomebases(): List<GetHomebaseResponse>{
        return homebaseService.getHomebases()
    }

    @DeleteMapping("/{homebaseId}/{tableId}")
    fun cancleHomebase(
        @PathVariable homebaseId: Long
    ) {
        homebaseService.cancleHomebase()
    }
}