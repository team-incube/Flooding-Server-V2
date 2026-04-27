package team.incube.flooding.domain.user.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.user.entity.Role
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
    ): Page<SearchUsersResponse> {
        val (start, end) = parseStudentNumberPrefix(studentNumber)
        return userRepository
            .searchUsers(
                name = name?.trim()?.takeIf { it.isNotEmpty() },
                studentNumberStart = start,
                studentNumberEnd = end,
                excludedRole = Role.ADMIN,
                pageable = pageable,
            ).map(SearchUsersResponse::from)
    }

    private fun parseStudentNumberPrefix(input: String?): Pair<Int?, Int?> {
        val trimmed = input?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.length > STUDENT_NUMBER_LENGTH || !trimmed.all(Char::isDigit)) {
            return null to null
        }
        val multiplier = POW10[STUDENT_NUMBER_LENGTH - trimmed.length]
        val base = trimmed.toInt() * multiplier
        return base to (base + multiplier - 1)
    }

    companion object {
        private const val STUDENT_NUMBER_LENGTH = 4
        private val POW10 = intArrayOf(1, 10, 100, 1000, 10000)
    }
}
