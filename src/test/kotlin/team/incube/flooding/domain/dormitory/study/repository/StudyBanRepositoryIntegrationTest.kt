package team.incube.flooding.domain.dormitory.study.repository

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.support.IntegrationTestBase
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Transactional
class StudyBanRepositoryIntegrationTest : IntegrationTestBase() {
    @Autowired
    lateinit var studyBanRepository: StudyBanJpaRepository

    @Autowired
    lateinit var userRepository: UserRepository

    private fun createUser(
        id: Long,
        email: String,
    ): UserJpaEntity =
        userRepository.save(
            UserJpaEntity(
                id = id,
                name = "테스트유저",
                sex = Sex.MAN,
                email = email,
                studentNumber = 10101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            ),
        )

    @Test
    fun `활성 밴이 존재하면 existsByUserIdAndBannedUntilAfter는 true를 반환한다`() {
        val user = createUser(1L, "user1@test.com")
        val now = LocalDateTime.now()
        studyBanRepository.save(
            StudyBanJpaEntity(
                user = user,
                bannedUntil = now.plusWeeks(1),
            ),
        )

        val result = studyBanRepository.existsByUserIdAndBannedUntilAfter(user.id, now)

        assertTrue(result)
    }

    @Test
    fun `만료된 밴만 존재하면 existsByUserIdAndBannedUntilAfter는 false를 반환한다`() {
        val user = createUser(2L, "user2@test.com")
        val now = LocalDateTime.now()
        studyBanRepository.save(
            StudyBanJpaEntity(
                user = user,
                bannedUntil = now.minusDays(1),
            ),
        )

        val result = studyBanRepository.existsByUserIdAndBannedUntilAfter(user.id, now)

        assertFalse(result)
    }

    @Test
    fun `활성 밴이 존재하면 findByUserIdAndBannedUntilAfter가 해당 엔티티를 반환한다`() {
        val user = createUser(3L, "user3@test.com")
        val now = LocalDateTime.now()
        val ban =
            studyBanRepository.save(
                StudyBanJpaEntity(
                    user = user,
                    bannedUntil = now.plusWeeks(1),
                ),
            )

        val result = studyBanRepository.findByUserIdAndBannedUntilAfter(user.id, now)

        assertNotNull(result)
        assertEquals(ban.id, result.id)
    }

    @Test
    fun `활성 밴이 없으면 findByUserIdAndBannedUntilAfter는 null을 반환한다`() {
        val user = createUser(4L, "user4@test.com")
        val now = LocalDateTime.now()
        studyBanRepository.save(
            StudyBanJpaEntity(
                user = user,
                bannedUntil = now.minusDays(1),
            ),
        )

        val result = studyBanRepository.findByUserIdAndBannedUntilAfter(user.id, now)

        assertNull(result)
    }

    @Test
    fun `findAllByUserIdInAndBannedUntilAfter는 활성 밴을 가진 유저만 반환한다`() {
        val user1 = createUser(5L, "user5@test.com")
        val user2 = createUser(6L, "user6@test.com")
        val user3 = createUser(7L, "user7@test.com")
        val now = LocalDateTime.now()

        studyBanRepository.save(
            StudyBanJpaEntity(user = user1, bannedUntil = now.plusWeeks(1)),
        )
        studyBanRepository.save(
            StudyBanJpaEntity(user = user2, bannedUntil = now.minusDays(1)),
        )

        val result =
            studyBanRepository.findAllByUserIdInAndBannedUntilAfter(
                listOf(user1.id, user2.id, user3.id),
                now,
            )

        assertEquals(1, result.size)
        assertEquals(user1.id, result.first().user.id)
    }
}
