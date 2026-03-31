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

        if (clubParticipantJpaRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw IllegalStateException("이미 해당 동아리에 속해 있습니다")
        }
        val club = clubRepository.findById(clubId)
            .orElseThrow { IllegalArgumentException("ID가 ${clubId}인 동아리를 찾을 수 없습니다.") }
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("ID가 ${userId}인 사용자를 찾을 수 없습니다.") }

        val newParticipant = ClubParticipantJpaEntity(
            club = club,
            user = user
        )

        clubParticipantJpaRepository.save(newParticipant)
    }
}