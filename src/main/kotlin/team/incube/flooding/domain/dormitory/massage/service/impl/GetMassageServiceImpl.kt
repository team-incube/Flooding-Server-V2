package team.incube.flooding.domain.dormitory.massage.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.presentation.data.response.GetMassageResponse
import team.incube.flooding.domain.dormitory.massage.service.GetMassageService
import team.incube.flooding.domain.user.repository.UserRepository

@Service
@Transactional(readOnly = true)
class GetMassageServiceImpl(
    private val massageRedisAdapter: MassageRedisAdapter,
    private val userRepository: UserRepository,
) : GetMassageService {
    override fun execute(): List<GetMassageResponse> {
        val queue = massageRedisAdapter.getQueue()
        if (queue.isEmpty()) return emptyList()
        val userMap = userRepository.findAllById(queue).associateBy { it.id }
        return queue
            .mapIndexed { index, userId ->
                val user = userMap[userId] ?: return@mapIndexed null
                GetMassageResponse(order = index + 1L, name = user.name, studentNumber = user.studentNumber)
            }.filterNotNull()
    }
}
