package team.incube.flooding.domain.club.event

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.global.client.DataGsmClubClient
import java.time.LocalDate

@Component
class ClubApprovedEventListener(
    private val clubRepository: ClubRepository,
    private val dataGsmClubClient: DataGsmClubClient,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleClubApproved(event: ClubApprovedEvent) {
        val club = clubRepository.findByIdWithLeader(event.clubId) ?: return

        val leaderDataGsmId = club.leader?.let { dataGsmClubClient.getStudentIdByEmail(it.email) }

        val dataGsmClubId =
            dataGsmClubClient.createClub(
                DataGsmClubClient.ClubReqDto(
                    name = club.name,
                    type = club.type.name,
                    leaderId = leaderDataGsmId,
                    participantIds = emptyList(),
                    foundedYear = LocalDate.now().year,
                    status = "ACTIVE",
                ),
            )

        if (dataGsmClubId != null) {
            club.dataGsmClubId = dataGsmClubId
        }
    }
}
