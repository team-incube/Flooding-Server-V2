package team.incube.flooding.domain.homebase.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.repository.HomebaseMemberRepository
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository
import java.time.LocalDate

@Service
class GetHomebaseReservationService(
    private val reservationRepository: HomebaseReservationRepository,
    private val memberRepository: HomebaseMemberRepository,
) {
    @Transactional(readOnly = true)
    fun getReservationList(): List<GetHomebaseResponse> {
        val reservations = reservationRepository.findAllWithHomebase()
        val membersByReservationId =
            memberRepository
                .findAllByReservationIdIn(reservations.map { it.id })
                .groupBy { it.reservation.id }
        return reservations.map { it.toResponse(membersByReservationId[it.id].orEmpty()) }
    }

    @Transactional(readOnly = true)
    fun getReservationList(reservationDate: LocalDate): List<GetHomebaseResponse> {
        val reservations = reservationRepository.findAllWithHomebaseByDate(reservationDate)
        val membersByReservationId =
            memberRepository
                .findAllByReservationIdIn(reservations.map { it.id })
                .groupBy { it.reservation.id }
        return reservations.map { it.toResponse(membersByReservationId[it.id].orEmpty()) }
    }
}
