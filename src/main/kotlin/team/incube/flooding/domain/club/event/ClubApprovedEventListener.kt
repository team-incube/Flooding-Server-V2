package team.incube.flooding.domain.club.event

import org.springframework.scheduling.annotation.Async
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
    private val clubDataGsmIdSaver: ClubDataGsmIdSaver,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
                    status = club.status.name,
                ),
            ) ?: return

        clubDataGsmIdSaver.save(event.clubId, dataGsmClubId)
    }
}

@Component
class ClubDataGsmIdSaver(
    private val clubRepository: ClubRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(
        clubId: Long,
        dataGsmClubId: Long,
    ) {
        val club = clubRepository.findById(clubId).orElse(null) ?: return
        club.dataGsmClubId = dataGsmClubId
    }
}
