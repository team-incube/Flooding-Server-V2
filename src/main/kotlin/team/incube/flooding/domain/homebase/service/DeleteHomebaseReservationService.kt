package team.incube.flooding.domain.homebase.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository

@Service
class DeleteHomebaseReservationService(
    private val reservationRepository: HomebaseReservationRepository,
    private val memberService: HomebaseMemberService
) {
    @Transactional
    fun deleteReservation(reservationId: Long) {
        memberService.deleteByReservationId(reservationId)
        reservationRepository.deleteById(reservationId)
    }
}