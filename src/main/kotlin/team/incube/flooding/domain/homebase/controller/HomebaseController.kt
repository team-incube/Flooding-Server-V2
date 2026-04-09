package team.incube.flooding.domain.homebase.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.dto.request.UpdateHomebaseMembersRequest
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.service.*
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "홈베이스", description = "홈베이스 예약 관련 API")
@RestController
@RequestMapping("/homebase")
class HomebaseController(
    private val createService: CreateHomebaseReservationService,
    private val patchService: PatchHomebaseReservationService,
    private val deleteService: DeleteHomebaseReservationService,
    private val getService: GetHomebaseReservationService
) {

    @Operation(summary = "홈베이스 예약 생성")
    @PostMapping("/{homebaseId}")
    fun createReservation(
        @PathVariable homebaseId: Long,
        @RequestBody @Valid request: CreateHomebaseRequest
    ): CommonApiResponse<Nothing> {
        createService.createReservation(homebaseId, request)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "홈베이스 예약 조회")
    @GetMapping
    fun getReservation(): CommonApiResponse<List<GetHomebaseResponse>> {
        val reservations = getService.getReservationList()
        return CommonApiResponse.success("OK", reservations)
    }

    @Operation(summary = "홈베이스 예약 삭제")
    @DeleteMapping("/{reservationId}")
    fun deleteReservation(
        @PathVariable reservationId: Long,
    ): CommonApiResponse<Nothing> {
        deleteService.deleteReservation(reservationId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "홈베이스 예약 수정")
    @PatchMapping("/{reservationId}")
    fun updateReservation(
        @PathVariable reservationId: Long,
        @RequestBody @Valid request: UpdateHomebaseMembersRequest
    ): CommonApiResponse<Nothing> {
        patchService.patchReservation(reservationId, request)
        return CommonApiResponse.success("OK")
    }
}