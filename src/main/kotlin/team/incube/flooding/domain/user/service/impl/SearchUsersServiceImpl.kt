package team.incube.flooding.domain.user.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.user.presentation.data.response.SearchUsersResponse
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.SearchUsersService

@Service
class SearchUsersServiceImpl(
    private val userRepository: UserRepository,
) : SearchUsersService {
    @Transactional(readOnly = true)
    override fun execute(
        name: String?,
        studentNumber: String?,
        pageable: Pageable,
    ): Page<SearchUsersResponse> =
        userRepository
            .searchUsers(
                name = name?.trim()?.takeIf { it.isNotEmpty() },
                studentNumber = studentNumber?.trim()?.takeIf { it.isNotEmpty() },
                pageable = pageable,
            ).map(SearchUsersResponse::from)
}
