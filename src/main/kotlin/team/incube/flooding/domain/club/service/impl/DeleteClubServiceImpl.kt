package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.DeleteClubService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteClubService {
    @Transactional
    override fun execute(clubId: Long) {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }

        val currentUser = currentUserProvider.getCurrentUser()
        val isAdminOrCouncil = currentUser.role == Role.ADMIN || currentUser.role == Role.STUDENT_COUNCIL
        val isLeader = club.leader?.id == currentUser.id

        if (!isAdminOrCouncil && !isLeader) {
            throw ExpectedException("동아리를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        clubRepository.deleteById(clubId)
    }
}
