package team.incube.flooding.domain.homebase.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.homebase.dto.request.UpdateHomebaseMembersRequest
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository

@Service
class PatchHomebaseReservationService(
    private val reservationRepository: HomebaseReservationRepository,
    private val memberService: HomebaseMemberService
) {
    @Transactional
    fun patchReservation(reservationId: Long, request: UpdateHomebaseMembersRequest) {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다.") }

        validateCapacity(reservation.homebase.capacity, request.members.size)
        memberService.validateStudentDuplicate(
            reservation.startPeriod,
            reservation.endPeriod,
            request.members,
            reservationId
        )

        memberService.deleteByReservationId(reservationId)
        memberService.saveAllMembers(reservation, request.members)
    }

    private fun validateCapacity(capacity: Int, memberCount: Int) {
        if (memberCount > capacity) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "정원을 초과했습니다.")
        }
    }
}