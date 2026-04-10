package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldType
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationAnswerRequest
import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationRequest
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.service.impl.CreateClubApplicationServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class CreateClubApplicationServiceTest : BehaviorSpec({
    val clubFormRepository = mockk<ClubFormRepository>()
    val clubFormFieldRepository = mockk<ClubFormFieldRepository>()
    val clubFormSubmissionRepository = mockk<ClubFormSubmissionRepository>()
    val clubFormAnswerRepository = mockk<ClubFormAnswerRepository>()
    val currentUserProvider = mockk<CurrentUserProvider>()

    val service = CreateClubApplicationServiceImpl(
        clubFormRepository,
        clubFormFieldRepository,
        clubFormSubmissionRepository,
        clubFormAnswerRepository,
        currentUserProvider,
    )

    val user = UserJpaEntity(
        id = 1L,
        name = "테스트",
        sex = Sex.MAN,
        email = "test@test.com",
        studentNumber = 10101,
        role = Role.GENERAL_STUDENT,
        dormitoryRoom = 101,
    )

    val club = ClubJpaEntity(
        id = 1L,
        name = "테스트 동아리",
        type = ClubType.MAJOR_CLUB,
        leader = null,
        imageUrl = null,
        status = ClubStatus.MAINTAIN,
    )

    val form = ClubFormJpaEntity(id = 10L, club = club, title = "신청 폼", description = null)

    val requiredField = ClubFormFieldJpaEntity(
        id = 1L,
        form = form,
        label = "자기소개",
        description = null,
        fieldType = ClubFormFieldType.TEXTAREA,
        fieldOrder = 1,
        isRequired = true,
    )

    val optionalField = ClubFormFieldJpaEntity(
        id = 2L,
        form = form,
        label = "특기사항",
        description = null,
        fieldType = ClubFormFieldType.TEXT,
        fieldOrder = 2,
        isRequired = false,
    )

    beforeTest {
        clearMocks(clubFormRepository, clubFormFieldRepository, clubFormSubmissionRepository, clubFormAnswerRepository, currentUserProvider)
        every { currentUserProvider.getCurrentUser() } returns user
    }

    given("활성 폼이 없을 때") {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns null

        `when`("execute를 호출하면") {
            then("NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<ExpectedException> {
                    service.execute(1L, CreateClubApplicationRequest(emptyList()))
                }
                exception.statusCode shouldBe HttpStatus.NOT_FOUND
            }
        }
    }

    given("이미 신청한 사용자가") {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns true

        `when`("다시 신청하면") {
            then("CONFLICT 예외가 발생한다") {
                val exception = shouldThrow<ExpectedException> {
                    service.execute(1L, CreateClubApplicationRequest(emptyList()))
                }
                exception.statusCode shouldBe HttpStatus.CONFLICT
            }
        }
    }

    given("필수 항목에 빈 값을 입력했을 때") {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(requiredField, optionalField)

        `when`("execute를 호출하면") {
            then("BAD_REQUEST 예외가 발생한다") {
                val request = CreateClubApplicationRequest(
                    answers = listOf(CreateClubApplicationAnswerRequest(fieldId = 1L, value = "")),
                )
                val exception = shouldThrow<ExpectedException> {
                    service.execute(1L, request)
                }
                exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

    given("정상적인 신청 요청이 올 때") {
        val submission = ClubFormSubmissionJpaEntity(id = 100L, form = form, user = user)
        val answersSlot = slot<List<ClubFormAnswerJpaEntity>>()

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(requiredField, optionalField)
        every { clubFormSubmissionRepository.save(any()) } returns submission
        every { clubFormAnswerRepository.saveAll(capture(answersSlot)) } returns emptyList()

        `when`("execute를 호출하면") {
            then("applicationId가 반환되고 saveAll이 호출된다") {
                val request = CreateClubApplicationRequest(
                    answers = listOf(
                        CreateClubApplicationAnswerRequest(fieldId = 1L, value = "열심히 하겠습니다"),
                        CreateClubApplicationAnswerRequest(fieldId = 2L, value = null),
                    ),
                )
                val response = service.execute(1L, request)
                response.applicationId shouldBe 100L
                verify { clubFormAnswerRepository.saveAll(any<List<ClubFormAnswerJpaEntity>>()) }
            }
        }
    }

    given("선택 항목만 있는 폼에") {
        val submission = ClubFormSubmissionJpaEntity(id = 100L, form = form, user = user)

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(optionalField)
        every { clubFormSubmissionRepository.save(any()) } returns submission
        every { clubFormAnswerRepository.saveAll(any<List<ClubFormAnswerJpaEntity>>()) } returns emptyList()

        `when`("답변 없이 신청하면") {
            then("정상 처리된다") {
                val response = service.execute(1L, CreateClubApplicationRequest(emptyList()))
                response.applicationId shouldBe 100L
            }
        }
    }
})
