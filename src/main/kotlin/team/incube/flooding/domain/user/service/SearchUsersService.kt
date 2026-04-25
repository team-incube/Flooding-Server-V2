package team.incube.flooding.domain.user.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.incube.flooding.domain.user.presentation.data.response.SearchUsersResponse

interface SearchUsersService {
    fun execute(
        name: String?,
        studentNumber: String?,
        pageable: Pageable,
    ): Page<SearchUsersResponse>
}
