package team.incube.flooding.domain.dormitory.massage.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.config.MassageProperties
import team.incube.flooding.domain.dormitory.massage.entity.MassageApplicationStatus
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageListResponse
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse
import team.incube.flooding.domain.dormitory.massage.service.GetMassageService
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class GetMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val userRepository: UserRepository,
    private val massageProperties: MassageProperties,
    private val currentUserProvider: CurrentUserProvider,
    private val clock: Clock,
) : GetMassageService {
    override fun execute(): GetMassageListResponse {
        val currentUser = currentUserProvider.getCurrentUser()
        val now = LocalTime.now(clock)
        val isApplicationOpen = !now.isBefore(massageProperties.openTime) && now.isBefore(massageProperties.closeTime)
        val myApplicationStatus =
            when {
                massageRedisAdapter.isReapplyBlocked(currentUser.id) -> MassageApplicationStatus.CANCELLED
                massageRedisAdapter.isApply(currentUser.id) -> MassageApplicationStatus.APPLIED
                else -> null
            }
        val queue = massageRedisAdapter.getQueue()
        val applicants =
            if (queue.isEmpty()) {
                emptyList()
            } else {
                val userMap = userRepository.findAllById(queue).associateBy { it.id }
                queue
                    .mapIndexed { index, userId ->
                        val user = userMap[userId] ?: return@mapIndexed null
                        GetMassageResponse(order = index + 1L, name = user.name, studentNumber = user.studentNumber)
                    }.filterNotNull()
            }
        return GetMassageListResponse(
            isApplicationOpen = isApplicationOpen,
            myApplicationStatus = myApplicationStatus,
            applicants = applicants,
        )
    }
}
