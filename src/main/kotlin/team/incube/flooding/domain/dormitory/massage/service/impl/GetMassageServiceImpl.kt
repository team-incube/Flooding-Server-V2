package team.incube.flooding.domain.dormitory.massage.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.config.MassageProperties
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageListResponse
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse
import team.incube.flooding.domain.dormitory.massage.service.GetMassageService
import team.incube.flooding.domain.user.repository.UserRepository
import java.time.Clock
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class GetMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val userRepository: UserRepository,
    private val massageProperties: MassageProperties,
    private val clock: Clock,
) : GetMassageService {
    override fun execute(): GetMassageListResponse {
        val isApplicationOpen = !LocalTime.now(clock).isBefore(massageProperties.openTime)
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
            applicants = applicants,
        )
    }
}
