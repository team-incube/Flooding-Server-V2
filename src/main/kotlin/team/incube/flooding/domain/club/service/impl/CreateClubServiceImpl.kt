package team.incube.flooding.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.presentation.data.request.CreateClubRequest
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.CreateClubService
import team.incube.flooding.global.security.util.CurrentUserProvider

@Service
class CreateClubServiceImpl(
    private val clubRepository: ClubRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateClubService {
    @Transactional
    override fun execute(request: CreateClubRequest) {
        val leader = currentUserProvider.getCurrentUser()

        clubRepository.save(
            ClubJpaEntity(
                name = request.name,
                type = request.type,
                leader = leader,
                imageUrl = request.imageUrl,
                status = request.status,
                description = request.description,
                maxMember = request.maxMember,
            ),
        )
    }
}
