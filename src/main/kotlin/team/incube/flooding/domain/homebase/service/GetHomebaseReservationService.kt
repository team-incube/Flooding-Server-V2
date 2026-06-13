package team.incube.flooding.domain.homebase.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository
import java.time.LocalDate

@Service
class GetHomebaseReservationService(
    private val reservationRepository: HomebaseReservationRepository,
) {
    @Transactional(readOnly = true)
    fun getReservationList(): List<GetHomebaseResponse> =
        reservationRepository.findAllWithMembers().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getReservationList(reservationDate: LocalDate): List<GetHomebaseResponse> =
        reservationRepository.findAllWithMembersByDate(reservationDate).map { it.toResponse() }
}
