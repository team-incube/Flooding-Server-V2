package team.incube.flooding.domain.homebase.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.repository.HomebaseRepository
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository

@Service
class CreateHomebaseReservationService(private val homebaseRepository: HomebaseRepository, private val reservationRepository: HomebaseReservationRepository, private val memberService: HomebaseMemberService) {
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