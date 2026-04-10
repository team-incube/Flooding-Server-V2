package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubAutonomousApplicationJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubAutonomousApplicationRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.CreateAutonomousClubApplicationServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional
import java.util.concurrent.TimeUnit

class CreateAutonomousClubApplicationServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val clubAutonomousApplicationRepository = mockk<ClubAutonomousApplicationRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val redissonClient = mockk<RedissonClient>()
        val lock = mockk<RLock>(relaxed = true)

        val service =
            CreateAutonomousClubApplicationServiceImpl(
                clubRepository,
                clubAutonomousApplicationRepository,
                currentUserProvider,
                redissonClient,
            )

        val user =
            UserJpaEntity(
                id = 1L,
                name = "테스트",
                sex = Sex.MAN,
                email = "test@test.com",
                studentNumber = 10101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        fun autonomousClub(maxMember: Int? = 10) =
            ClubJpaEntity(
                id = 1L,
                name = "자율 동아리",
                type = ClubType.AUTONOMOUS_CLUB,
                leader = null,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = null,
                maxMember = maxMember,
            )

        val majorClub =
            ClubJpaEntity(
                id = 2L,
                name = "전공 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = null,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = null,
                maxMember = null,
            )

        given("존재하지 않는 동아리일 때") {
            `when`("신청하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(99L) } returns Optional.empty()

                    val exception = shouldThrow<ExpectedException> { service.execute(99L) }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("MAJOR_CLUB에") {
            `when`("자율 동아리 신청 API를 호출하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(2L) } returns Optional.of(majorClub)

                    val exception = shouldThrow<ExpectedException> { service.execute(2L) }
                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("maxMember가 설정되지 않은 자율 동아리일 때") {
            `when`("신청하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(1L) } returns Optional.of(autonomousClub(maxMember = null))
                    every { redissonClient.getLock(any<String>()) } returns lock
                    every { lock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }
                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("락 획득에 실패했을 때") {
            `when`("신청하면") {
                then("TOO_MANY_REQUESTS 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(1L) } returns Optional.of(autonomousClub())
                    every { redissonClient.getLock(any<String>()) } returns lock
                    every { lock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns false

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }
                    exception.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
                }
            }
        }

        given("이미 신청한 사용자가") {
            `when`("다시 신청하면") {
                then("CONFLICT 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(1L) } returns Optional.of(autonomousClub())
                    every { redissonClient.getLock(any<String>()) } returns lock
                    every { lock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true

                    every { clubAutonomousApplicationRepository.existsByClubIdAndUserId(1L, 1L) } returns true

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }
                    exception.statusCode shouldBe HttpStatus.CONFLICT
                }
            }
        }

        given("정원이 마감된 동아리에") {
            `when`("신청하면") {
                then("CONFLICT 예외가 발생한다") {
                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(1L) } returns Optional.of(autonomousClub(maxMember = 5))
                    every { redissonClient.getLock(any<String>()) } returns lock
                    every { lock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true

                    every { clubAutonomousApplicationRepository.existsByClubIdAndUserId(1L, 1L) } returns false
                    every { clubAutonomousApplicationRepository.countByClubId(1L) } returns 5L

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }
                    exception.statusCode shouldBe HttpStatus.CONFLICT
                }
            }
        }

        given("정상적인 신청 요청이 올 때") {
            `when`("신청하면") {
                then("applicationId가 반환되고 save가 호출된다") {
                    val club = autonomousClub(maxMember = 10)
                    val savedApplication = ClubAutonomousApplicationJpaEntity(id = 100L, club = club, user = user)

                    every { currentUserProvider.getCurrentUser() } returns user
                    every { clubRepository.findById(1L) } returns Optional.of(club)
                    every { redissonClient.getLock(any<String>()) } returns lock
                    every { lock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true

                    every { clubAutonomousApplicationRepository.existsByClubIdAndUserId(1L, 1L) } returns false
                    every { clubAutonomousApplicationRepository.countByClubId(1L) } returns 3L
                    every { clubAutonomousApplicationRepository.save(any()) } returns savedApplication

                    val response = service.execute(1L)
                    response.applicationId shouldBe 100L
                    verify(exactly = 1) { clubAutonomousApplicationRepository.save(any()) }
                }
            }
        }
    })
