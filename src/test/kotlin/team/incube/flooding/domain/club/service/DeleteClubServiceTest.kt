package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubParticipantRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.impl.DeleteClubServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.Optional

class DeleteClubServiceTest :
    BehaviorSpec({
        val clubRepository = mockk<ClubRepository>()
        val clubParticipantRepository = mockk<ClubParticipantRepository>()
        val clubFormRepository = mockk<ClubFormRepository>()
        val clubFormFieldRepository = mockk<ClubFormFieldRepository>()
        val clubFormFieldOptionRepository = mockk<ClubFormFieldOptionRepository>()
        val clubFormSubmissionRepository = mockk<ClubFormSubmissionRepository>()
        val clubFormAnswerRepository = mockk<ClubFormAnswerRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service =
            DeleteClubServiceImpl(
                clubRepository,
                clubParticipantRepository,
                clubFormRepository,
                clubFormFieldRepository,
                clubFormFieldOptionRepository,
                clubFormSubmissionRepository,
                clubFormAnswerRepository,
                currentUserProvider,
            )

        beforeEach { clearAllMocks() }

        fun user(
            id: Long,
            role: Role,
        ) = UserJpaEntity(
            id = id,
            name = "테스트",
            sex = Sex.MAN,
            email = "test@test.com",
            studentNumber = 10101,
            role = role,
            dormitoryRoom = 101,
        )

        fun club(leader: UserJpaEntity?) =
            ClubJpaEntity(
                id = 1L,
                name = "테스트 동아리",
                type = ClubType.MAJOR_CLUB,
                leader = leader,
                imageUrl = null,
                status = ClubStatus.MAINTAIN,
                description = null,
                maxMember = null,
            )

        fun mockDeleteDependencies() {
            justRun { clubFormAnswerRepository.deleteAllByClubId(1L) }
            justRun { clubFormSubmissionRepository.deleteAllByClubId(1L) }
            justRun { clubFormFieldOptionRepository.deleteAllByClubId(1L) }
            justRun { clubFormFieldRepository.deleteAllByClubId(1L) }
            justRun { clubFormRepository.deleteAllByClubId(1L) }
            justRun { clubParticipantRepository.deleteAllByClubId(1L) }
        }

        given("존재하지 않는 clubId로 요청할 때") {
            `when`("삭제하면") {
                then("NOT_FOUND 예외가 발생한다") {
                    every { clubRepository.findById(99L) } returns Optional.empty()
                    every { currentUserProvider.getCurrentUser() } returns user(1L, Role.ADMIN)

                    val exception = shouldThrow<ExpectedException> { service.execute(99L) }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        given("권한이 없는 일반 학생이") {
            `when`("본인 동아리가 아닌 동아리를 삭제하면") {
                then("FORBIDDEN 예외가 발생한다") {
                    val leader = user(2L, Role.GENERAL_STUDENT)
                    val currentUser = user(1L, Role.GENERAL_STUDENT)
                    every { clubRepository.findById(1L) } returns Optional.of(club(leader))
                    every { currentUserProvider.getCurrentUser() } returns currentUser

                    val exception = shouldThrow<ExpectedException> { service.execute(1L) }
                    exception.statusCode shouldBe HttpStatus.FORBIDDEN
                }
            }
        }

        given("동아리 리더가") {
            `when`("본인 동아리를 삭제하면") {
                then("삭제가 성공한다") {
                    val leader = user(1L, Role.GENERAL_STUDENT)
                    every { clubRepository.findById(1L) } returns Optional.of(club(leader))
                    every { currentUserProvider.getCurrentUser() } returns leader
                    mockDeleteDependencies()
                    justRun { clubRepository.delete(any()) }

                    service.execute(1L)

                    verify(exactly = 1) { clubFormAnswerRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubFormSubmissionRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubFormFieldOptionRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubFormFieldRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubFormRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubParticipantRepository.deleteAllByClubId(1L) }
                    verify(exactly = 1) { clubRepository.delete(any()) }
                }
            }
        }

        given("ADMIN이") {
            `when`("동아리를 삭제하면") {
                then("삭제가 성공한다") {
                    val admin = user(1L, Role.ADMIN)
                    every { clubRepository.findById(1L) } returns Optional.of(club(null))
                    every { currentUserProvider.getCurrentUser() } returns admin
                    mockDeleteDependencies()
                    justRun { clubRepository.delete(any()) }

                    service.execute(1L)

                    verify(exactly = 1) { clubRepository.delete(any()) }
                }
            }
        }

        given("STUDENT_COUNCIL이") {
            `when`("동아리를 삭제하면") {
                then("삭제가 성공한다") {
                    val council = user(1L, Role.STUDENT_COUNCIL)
                    every { clubRepository.findById(1L) } returns Optional.of(club(null))
                    every { currentUserProvider.getCurrentUser() } returns council
                    mockDeleteDependencies()
                    justRun { clubRepository.delete(any()) }

                    service.execute(1L)

                    verify(exactly = 1) { clubRepository.delete(any()) }
                }
            }
        }
    })
