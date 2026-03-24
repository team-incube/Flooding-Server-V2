package team.incube.flooding.domain.homebase.service

import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.dto.request.UpdateHomebaseMembersRequest
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.repository.HomebaseRepository
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository

@Service
class HomebaseReservationService(

    private val homebaseRepository: HomebaseRepository,
    private val reservationRepository: HomebaseReservationRepository,
    private val memberService: HomebaseMemberService

) {

    @Transactional
    fun createReservation(homebaseId: Long, request: CreateHomebaseRequest) {
        val homebase = homebaseRepository.findById(homebaseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "홈베이스가 존재하지 않습니다.") }

        validateCapacity(homebase.capacity, request.members.size)
        validateReservationOverlap(homebase.id, request.startPeriod, request.endPeriod)

        memberService.validateStudentDuplicate(request.startPeriod, request.endPeriod, request.members)

        val reservation = reservationRepository.save(
            HomebaseReservationJpaEntity(
                startPeriod = request.startPeriod,
                endPeriod = request.endPeriod,
                homebase = homebase
            )
        )

        memberService.saveAllMembers(reservation, request.members)
    }

    @Transactional(readOnly = true)
    fun getReservationList(): List<GetHomebaseResponse> {
        return reservationRepository.findAllWithMembers().map { it.toResponse() }
    }

    @Transactional
    fun deleteReservation(reservationId: Long) {
        memberService.deleteByReservationId(reservationId)
        reservationRepository.deleteById(reservationId)
    }

    @Transactional
    fun updateReservationMembers(reservationId: Long, request: UpdateHomebaseMembersRequest) {
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

    private fun validateReservationOverlap(homebaseId: Long, start: Int, end: Int) {
        val overlapping = reservationRepository.findOverlappingReservation(homebaseId, start, end)
        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("이미 예약된 시간입니다.")
        }
    }
}