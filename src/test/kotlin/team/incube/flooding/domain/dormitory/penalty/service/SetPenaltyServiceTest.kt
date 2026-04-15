package team.incube.flooding.domain.dormitory.penalty.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * 기숙사 벌점 설정 서비스 로직 테스트
 *
 * SetPenaltyServiceImpl의 핵심 로직을 인메모리로 시뮬레이션하여 검증합니다.
 */
class SetPenaltyServiceTest {
    private data class PenaltyHistory(
        val previousScore: Int,
        val newScore: Int,
        val reason: String,
    )

    private fun setPenaltyLogic(
        users: MutableMap<Long, Int>,
        histories: MutableList<PenaltyHistory>,
        userId: Long,
        score: Int,
        reason: String,
    ) {
        val previousScore = users[userId] ?: throw RuntimeException("존재하지 않는 유저입니다.")
        users[userId] = score
        histories.add(PenaltyHistory(previousScore = previousScore, newScore = score, reason = reason))
    }

    @Test
    fun `존재하지 않는 유저에 벌점 설정 시 예외가 발생한다`() {
        val users = mutableMapOf<Long, Int>()
        val histories = mutableListOf<PenaltyHistory>()

        assertThrows(RuntimeException::class.java) {
            setPenaltyLogic(users, histories, 999L, 5, "테스트 사유")
        }
    }

    @Test
    fun `벌점 설정 시 변경 전 점수와 새 점수가 이력에 기록된다`() {
        val users = mutableMapOf(1L to 3)
        val histories = mutableListOf<PenaltyHistory>()

        setPenaltyLogic(users, histories, 1L, 5, "지각")

        assertEquals(5, users[1L])
        assertEquals(1, histories.size)
        assertEquals(3, histories[0].previousScore)
        assertEquals(5, histories[0].newScore)
        assertEquals("지각", histories[0].reason)
    }

    @Test
    fun `동일한 점수로 설정해도 이력이 기록된다`() {
        val users = mutableMapOf(1L to 3)
        val histories = mutableListOf<PenaltyHistory>()

        setPenaltyLogic(users, histories, 1L, 3, "동일 점수 확인")

        assertEquals(3, users[1L])
        assertEquals(1, histories.size)
        assertEquals(3, histories[0].previousScore)
        assertEquals(3, histories[0].newScore)
    }

    @Test
    fun `벌점을 0으로 초기화할 수 있다`() {
        val users = mutableMapOf(1L to 10)
        val histories = mutableListOf<PenaltyHistory>()

        setPenaltyLogic(users, histories, 1L, 0, "벌점 초기화")

        assertEquals(0, users[1L])
        assertEquals(10, histories[0].previousScore)
        assertEquals(0, histories[0].newScore)
    }
}
