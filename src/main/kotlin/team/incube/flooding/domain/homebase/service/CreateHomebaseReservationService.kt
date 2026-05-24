package team.incube.flooding.domain.homebase.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.repository.HomebaseRepository
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDate

@Service
class CreateHomebaseReservationService(
    private val homebaseRepository: HomebaseRepository,
    private val reservationRepository: HomebaseReservationRepository,
    private val memberService: HomebaseMemberService,
) {
    @Transactional
    fun createReservation(
        homebaseId: Long,
        request: CreateHomebaseRequest,
    ) {
        val homebase =
            homebaseRepository
                .findById(homebaseId)
                .orElseThrow { ExpectedException("홈베이스가 존재하지 않습니다.", HttpStatus.NOT_FOUND) }

        validateCapacity(homebase.capacity, request.members.size)
        validateReservationOverlap(homebase.id, request.reservationDate, request.startPeriod, request.endPeriod)
        memberService.validateStudentDuplicate(
            request.reservationDate,
            request.startPeriod,
            request.endPeriod,
            request.members,
        )

        val reservation =
            reservationRepository.save(
                HomebaseReservationJpaEntity(
                    reservationDate = request.reservationDate,
                    startPeriod = request.startPeriod,
                    endPeriod = request.endPeriod,
                    reason = request.reason,
                    homebase = homebase,
                ),
            )
        memberService.saveAllMembers(reservation, request.members)
    }

    private fun validateCapacity(
        capacity: Int,
        memberCount: Int,
    ) {
        if (memberCount > capacity) {
            throw ExpectedException("정원을 초과했습니다.", HttpStatus.BAD_REQUEST)
        }
    }

    private fun validateReservationOverlap(
        homebaseId: Long,
        reservationDate: LocalDate,
        start: Int,
        end: Int,
    ) {
        val overlapping = reservationRepository.findOverlappingReservation(homebaseId, reservationDate, start, end)
        if (overlapping.isNotEmpty()) {
            throw ExpectedException("이미 예약된 시간입니다.", HttpStatus.BAD_REQUEST)
        }
    }
}
