package team.incube.flooding.domain.dormitory.massage.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.dormitory.massage.adapter.MassageRedisAdapter
import team.incube.flooding.domain.dormitory.massage.config.MassageProperties
import team.incube.flooding.domain.dormitory.massage.entity.MassageApplicationStatus
import team.incube.flooding.domain.dormitory.massage.service.impl.GetMassageServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class GetMassageServiceTest :
    BehaviorSpec({
        val massageRedisAdapter = mockk<MassageRedisAdapter>()
        val userRepository = mockk<UserRepository>()
        val massageProperties =
            MassageProperties(
                openTime = LocalTime.of(20, 0),
                closeTime = LocalTime.of(22, 0),
                maxCount = 5,
                lockKey = "massage:lock",
            )
        val currentUserProvider = mockk<CurrentUserProvider>()
        val clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("Asia/Seoul"))

        fun user(id: Long) =
            UserJpaEntity(
                id = id,
                name = "학생$id",
                sex = Sex.MAN,
                email = "s$id@test.com",
                studentNumber = (1000 + id).toInt(),
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        val currentUser = user(999L)
        every { currentUserProvider.getCurrentUser() } returns currentUser
        every { massageRedisAdapter.isReapplyBlocked(any()) } returns false

        val service =
            GetMassageServiceImpl(
                massageRedisAdapter = massageRedisAdapter,
                userRepository = userRepository,
                massageProperties = massageProperties,
                currentUserProvider = currentUserProvider,
                clock = clock,
            )

        given("5명이 순서대로 신청했을 때 (신청 순서: userId 1→2→3→4→5)") {
            `when`("목록을 조회하면") {
                then("제일 먼저 신청한 userId=1의 order가 1이다") {
                    every { massageRedisAdapter.getQueue() } returns listOf(1L, 2L, 3L, 4L, 5L)
                    every { userRepository.findAllById(any<List<Long>>()) } returns
                        listOf(user(1), user(2), user(3), user(4), user(5))

                    val result = service.execute()

                    result.applicants[0].order shouldBe 1L
                    result.applicants[1].order shouldBe 2L
                    result.applicants[2].order shouldBe 3L
                    result.applicants[3].order shouldBe 4L
                    result.applicants[4].order shouldBe 5L
                }
            }
        }

        given("현재 사용자가 이미 대기열에 포함되어 있을 때") {
            `when`("목록을 조회하면") {
                then("myApplicationStatus가 APPLIED이다") {
                    clearMocks(massageRedisAdapter, answers = false)
                    every { massageRedisAdapter.isReapplyBlocked(any()) } returns false
                    every { massageRedisAdapter.getQueue() } returns listOf(currentUser.id)
                    every { userRepository.findAllById(any<List<Long>>()) } returns listOf(currentUser)

                    val result = service.execute()

                    result.myApplicationStatus shouldBe MassageApplicationStatus.APPLIED
                    verify(exactly = 1) { massageRedisAdapter.getQueue() }
                }
            }
        }
    })
