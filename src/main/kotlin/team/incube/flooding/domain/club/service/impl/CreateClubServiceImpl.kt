package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.CreateClubService
import team.incube.flooding.domain.user.repository.UserRepository
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val userRepository: UserRepository,
) : CreateClubService {
    @Transactional
    override fun execute(request: CreateClubRequest) {
        val leader =
            userRepository.findById(request.leaderId).orElseThrow {
                ExpectedException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND)
            }

        clubRepository.save(
            ClubJpaEntity(
                name = request.name,
                type = request.type,
                leader = leader,
                imageUrl = request.imageUrl,
                status = ClubStatus.NEW,
                description = request.description,
                maxMember = request.maxMember,
            ),
        )
    }
}
