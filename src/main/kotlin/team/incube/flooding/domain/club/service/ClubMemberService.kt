package team.incube.flooding.domain.club.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubParticipantId
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.user.repository.UserRepository

@Service
class ClubMemberService(
    private val clubRepository: ClubJpaRepository,
    private val userRepository: UserRepository,
    private val clubParticipantJpaRepository: ClubParticipantJpaRepository
) {

    @Transactional
    fun inviteMember(clubId: Long, userId: Long) {

        val participantId = ClubParticipantId(club = clubId, user = userId)
        if (clubParticipantJpaRepository.existsById(participantId)) {
            throw IllegalStateException("이미 해당 동아리에 속해 있습니다")
        }

        val clubRef = clubRepository.getReferenceById(clubId)
        val userRef = userRepository.getReferenceById(userId)

        val newParticipant = ClubParticipantJpaEntity(
            club = clubRef,
            user = userRef
        )

        clubParticipantJpaRepository.save(newParticipant)
    }
}