package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.presentation.data.request.PutClubRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.PutClubService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class PutClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val currentUserProvider: CurrentUserProvider,
) : PutClubService {
    @Transactional
    override fun execute(
        clubId: Long,
        request: PutClubRequest,
    ) {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }

        val currentUser = currentUserProvider.getCurrentUser()

        if (!club.isModifiableBy(currentUser)) {
            throw ExpectedException("동아리를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        club.name = request.name
        club.description = request.description
        club.imageUrl = request.imageUrl
        club.maxMember = request.maxMember
    }
}
