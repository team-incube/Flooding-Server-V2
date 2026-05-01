package team.incube.flooding.domain.user.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.flooding.domain.club.repository.ClubAutonomousApplicationRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.dormitory.study.repository.StudyBanJpaRepository
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.service.impl.GetMeServiceImpl
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class GetMeServiceTest :
    BehaviorSpec({
        val currentUserProvider = mockk<CurrentUserProvider>()
        val studyBanJpaRepository = mockk<StudyBanJpaRepository>()
        val clubFormSubmissionRepository = mockk<ClubFormSubmissionRepository>()
        val clubAutonomousApplicationRepository = mockk<ClubAutonomousApplicationRepository>()
        val clock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneId.of("UTC"))

        val service =
            GetMeServiceImpl(
                currentUserProvider,
                studyBanJpaRepository,
                clubFormSubmissionRepository,
                clubAutonomousApplicationRepository,
                clock,
            )

        fun user() =
            UserJpaEntity(
                id = 1L,
                name = "테스트",
                sex = Sex.MAN,
                email = "test@test.com",
                studentNumber = 10101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        fun stubUserContext(user: UserJpaEntity) {
            every { currentUserProvider.getCurrentUser() } returns user
            every { studyBanJpaRepository.existsByUserIdAndBannedUntilAfter(user.id, any()) } returns false
        }

        beforeTest {
            clearMocks(
                currentUserProvider,
                studyBanJpaRepository,
                clubFormSubmissionRepository,
                clubAutonomousApplicationRepository,
            )
        }

        given("정규 동아리 신청 기록이 있는 사용자가") {
            `when`("내 정보를 조회하면") {
                then("hasClubApplication이 true다") {
                    val currentUser = user()
                    stubUserContext(currentUser)
                    every { clubFormSubmissionRepository.existsByUserId(currentUser.id) } returns true

                    val response = service.execute()

                    response.hasClubApplication shouldBe true
                }
            }
        }

        given("자율 동아리 신청 기록이 있는 사용자가") {
            `when`("내 정보를 조회하면") {
                then("hasClubApplication이 true다") {
                    val currentUser = user()
                    stubUserContext(currentUser)
                    every { clubFormSubmissionRepository.existsByUserId(currentUser.id) } returns false
                    every { clubAutonomousApplicationRepository.existsByUserId(currentUser.id) } returns true

                    val response = service.execute()

                    response.hasClubApplication shouldBe true
                }
            }
        }

        given("동아리 신청 기록이 없는 사용자가") {
            `when`("내 정보를 조회하면") {
                then("hasClubApplication이 false다") {
                    val currentUser = user()
                    stubUserContext(currentUser)
                    every { clubFormSubmissionRepository.existsByUserId(currentUser.id) } returns false
                    every { clubAutonomousApplicationRepository.existsByUserId(currentUser.id) } returns false

                    val response = service.execute()

                    response.hasClubApplication shouldBe false
                }
            }
        }
    })
