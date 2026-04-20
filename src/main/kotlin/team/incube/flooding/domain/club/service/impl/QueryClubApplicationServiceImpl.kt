package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.dto.response.ClubApplicationListResponse
import team.incube.flooding.domain.club.dto.response.ClubApplicationResponse
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.service.QueryClubApplicationService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryClubApplicationServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryClubApplicationService {
    @Transactional(readOnly = true)
    override fun execute(): ClubApplicationListResponse {
        val user = currentUserProvider.getCurrentUser()

        if (user.role != Role.ADMIN) {
            throw ExpectedException("동아리 개설 신청 목록을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        val clubs = clubJpaRepository.findAllByStatus(ClubStatus.NEW)

        return ClubApplicationListResponse(
            clubs =
                clubs.map { club ->
                    ClubApplicationResponse(
                        id = club.id,
                        name = club.name,
                        leader =
                            club.leader?.name
                                ?: throw ExpectedException("동아리장 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
                        type = club.type,
                        description = club.description ?: "",
                        imageUrl = club.imageUrl,
                        maxMember = club.maxMember,
                    )
                },
        )
    }
}
