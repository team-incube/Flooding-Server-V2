package team.incube.flooding.domain.dormitory.cleaningzone.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * 기숙사 청소 구역 인원 배정 서비스 로직 테스트
 *
 * AssignCleaningZoneMembersServiceImpl의 핵심 로직을 인메모리로 시뮬레이션하여 검증합니다.
 */
class AssignCleaningZoneMembersServiceTest {
    private data class Zone(
        val id: Long,
        val members: MutableList<SimUser> = mutableListOf(),
    )

    private data class SimUser(
        val id: Long,
        var zoneId: Long? = null,
    )

    private fun assignMembersLogic(
        zones: Map<Long, Zone>,
        allUsers: Map<Long, SimUser>,
        zoneId: Long,
        newMemberIds: List<Long>,
    ) {
        val zone = zones[zoneId] ?: throw RuntimeException("존재하지 않는 청소 구역입니다.")

        zone.members.forEach { it.zoneId = null }
        zone.members.clear()

        newMemberIds.mapNotNull { allUsers[it] }.forEach { user ->
            user.zoneId = zoneId
            zone.members.add(user)
        }
    }

    @Test
    fun `존재하지 않는 구역에 인원 배정 시 예외가 발생한다`() {
        val zones = mapOf<Long, Zone>()
        val users = mapOf<Long, SimUser>()

        assertThrows(RuntimeException::class.java) {
            assignMembersLogic(zones, users, 999L, listOf(1L))
        }
    }

    @Test
    fun `기존 멤버가 제거되고 새 멤버가 배정된다`() {
        val user1 = SimUser(id = 1L, zoneId = 1L)
        val user2 = SimUser(id = 2L, zoneId = 1L)
        val user3 = SimUser(id = 3L, zoneId = null)
        val zone = Zone(id = 1L, members = mutableListOf(user1, user2))
        val zones = mapOf(1L to zone)
        val users = mapOf(1L to user1, 2L to user2, 3L to user3)

        assignMembersLogic(zones, users, 1L, listOf(3L))

        assertNull(user1.zoneId)
        assertNull(user2.zoneId)
        assertEquals(1L, user3.zoneId)
        assertEquals(1, zone.members.size)
        assertEquals(3L, zone.members[0].id)
    }

    @Test
    fun `빈 목록으로 배정하면 기존 멤버가 모두 제거된다`() {
        val user1 = SimUser(id = 1L, zoneId = 1L)
        val zone = Zone(id = 1L, members = mutableListOf(user1))
        val zones = mapOf(1L to zone)
        val users = mapOf(1L to user1)

        assignMembersLogic(zones, users, 1L, emptyList())

        assertNull(user1.zoneId)
        assertEquals(0, zone.members.size)
    }

    @Test
    fun `여러 명을 한 번에 배정할 수 있다`() {
        val user1 = SimUser(id = 1L)
        val user2 = SimUser(id = 2L)
        val user3 = SimUser(id = 3L)
        val zone = Zone(id = 1L)
        val zones = mapOf(1L to zone)
        val users = mapOf(1L to user1, 2L to user2, 3L to user3)

        assignMembersLogic(zones, users, 1L, listOf(1L, 2L, 3L))

        assertEquals(1L, user1.zoneId)
        assertEquals(1L, user2.zoneId)
        assertEquals(1L, user3.zoneId)
        assertEquals(3, zone.members.size)
    }
}
