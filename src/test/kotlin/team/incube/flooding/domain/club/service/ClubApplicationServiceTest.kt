package team.incube.flooding.domain.club.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubJpaRepository
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.service.impl.ClubApplicationServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.util.Optional

class ClubApplicationServiceTest :
    BehaviorSpec({
        val clubJpaRepository = mockk<ClubJpaRepository>()
        val userRepository = mockk<UserRepository>()
        val clubFormRepository = mockk<ClubFormRepository>()
        val clubFormSubmissionRepository = mockk<ClubFormSubmissionRepository>()
        val clubFormAnswerRepository = mockk<ClubFormAnswerRepository>()
        val clubParticipantJpaRepository = mockk<ClubParticipantJpaRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service =
            ClubApplicationServiceImpl(
                clubJpaRepository,
                userRepository,
                clubFormRepository,
                clubFormSubmissionRepository,
                clubFormAnswerRepository,
                clubParticipantJpaRepository,
                currentUserProvider,
            )

        beforeEach { clearAllMocks() }

        fun user(id: Long) =
            UserJpaEntity(
                id = id,
                name = "테스트$id",
                sex = Sex.MAN,
                email = "test$id@test.com",
                studentNumber = 10100 + id.toInt(),
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        fun club(leader: UserJpaEntity) =
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

        given("동아리 리더가 신청자를 승인할 때") {
            `when`("신청 제출 데이터가 존재하면") {
                then("동아리원으로 추가하고 신청 목록에서 제거한다") {
                    val leader = user(1L)
                    val applicant = user(2L)
                    val club = club(leader)
                    val form = ClubFormJpaEntity(id = 10L, club = club, title = "신청 폼", description = null)
                    val submission = ClubFormSubmissionJpaEntity(id = 100L, form = form, user = applicant)

                    every { clubJpaRepository.findById(1L) } returns Optional.of(club)
                    every { currentUserProvider.getCurrentUser() } returns leader
                    every { clubParticipantJpaRepository.existsById(any()) } returns false
                    every { userRepository.findById(2L) } returns Optional.of(applicant)
                    every { clubParticipantJpaRepository.save(any()) } answers { firstArg<ClubParticipantJpaEntity>() }
                    every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
                    every { clubFormSubmissionRepository.findByFormIdAndUserId(10L, 2L) } returns submission
                    justRun { clubFormAnswerRepository.deleteAllBySubmissionId(100L) }
                    justRun { clubFormSubmissionRepository.delete(submission) }

                    service.execute(1L, 2L)

                    verify(exactly = 1) { clubParticipantJpaRepository.save(any()) }
                    verify(exactly = 1) { clubFormAnswerRepository.deleteAllBySubmissionId(100L) }
                    verify(exactly = 1) { clubFormSubmissionRepository.delete(submission) }
                }
            }
        }
    })
