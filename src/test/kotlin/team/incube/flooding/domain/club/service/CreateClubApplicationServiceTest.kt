package team.incube.flooding.domain.club.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

class CreateClubApplicationServiceTest {
    private val clubFormRepository = mockk<ClubFormRepository>()
    private val clubFormFieldRepository = mockk<ClubFormFieldRepository>()
    private val clubFormSubmissionRepository = mockk<ClubFormSubmissionRepository>()
    private val clubFormAnswerRepository = mockk<ClubFormAnswerRepository>()
    private val currentUserProvider = mockk<CurrentUserProvider>()

    private val service =
        CreateClubApplicationServiceImpl(
            clubFormRepository,
            clubFormFieldRepository,
            clubFormSubmissionRepository,
            clubFormAnswerRepository,
            currentUserProvider,
        )

    private val user =
        UserJpaEntity(
            id = 1L,
            name = "테스트",
            sex = Sex.MAN,
            email = "test@test.com",
            studentNumber = 10101,
            role = Role.GENERAL_STUDENT,
            dormitoryRoom = 101,
        )

    private val club =
        ClubJpaEntity(
            id = 1L,
            name = "테스트 동아리",
            type = ClubType.MAJOR_CLUB,
            leader = null,
            imageUrl = null,
            status = ClubStatus.MAINTAIN,
        )

    private val form = ClubFormJpaEntity(id = 10L, club = club, title = "신청 폼", description = null)

    private val requiredField =
        ClubFormFieldJpaEntity(
            id = 1L,
            form = form,
            label = "자기소개",
            description = null,
            fieldType = ClubFormFieldType.TEXTAREA,
            fieldOrder = 1,
            isRequired = true,
        )

    private val optionalField =
        ClubFormFieldJpaEntity(
            id = 2L,
            form = form,
            label = "특기사항",
            description = null,
            fieldType = ClubFormFieldType.TEXT,
            fieldOrder = 2,
            isRequired = false,
        )

    @BeforeEach
    fun setUp() {
        every { currentUserProvider.getCurrentUser() } returns user
    }

    @Test
    fun `활성_폼이_없으면_NOT_FOUND_예외가_발생한다`() {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns null

        val exception =
            assertThrows(ExpectedException::class.java) {
                service.execute(1L, CreateClubApplicationRequest(emptyList()))
            }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `중복_신청_시_CONFLICT_예외가_발생한다`() {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns true

        val exception =
            assertThrows(ExpectedException::class.java) {
                service.execute(1L, CreateClubApplicationRequest(emptyList()))
            }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
    }

    @Test
    fun `필수_항목_미입력_시_BAD_REQUEST_예외가_발생한다`() {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns
            listOf(requiredField, optionalField)

        val request =
            CreateClubApplicationRequest(answers = listOf(CreateClubApplicationAnswerRequest(fieldId = 1L, value = "")))

        val exception =
            assertThrows(ExpectedException::class.java) {
                service.execute(1L, request)
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun `정상_신청_시_applicationId가_반환된다`() {
        val submission = ClubFormSubmissionJpaEntity(id = 100L, form = form, user = user)
        val answersSlot = slot<List<ClubFormAnswerJpaEntity>>()

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns
            listOf(requiredField, optionalField)
        every { clubFormSubmissionRepository.save(any()) } returns submission
        every { clubFormAnswerRepository.saveAll(capture(answersSlot)) } returns emptyList()

        val request =
            CreateClubApplicationRequest(
                answers =
                    listOf(
                        CreateClubApplicationAnswerRequest(fieldId = 1L, value = "열심히 하겠습니다"),
                        CreateClubApplicationAnswerRequest(fieldId = 2L, value = null),
                    ),
            )

        val response = service.execute(1L, request)

        assertEquals(100L, response.applicationId)
        verify { clubFormAnswerRepository.saveAll(any<List<ClubFormAnswerJpaEntity>>()) }
    }

    @Test
    fun `선택_항목만_있는_폼은_답변_없이도_신청_가능하다`() {
        val submission = ClubFormSubmissionJpaEntity(id = 100L, form = form, user = user)

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormSubmissionRepository.existsByFormIdAndUserId(10L, 1L) } returns false
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(optionalField)
        every { clubFormSubmissionRepository.save(any()) } returns submission
        every { clubFormAnswerRepository.saveAll(any<List<ClubFormAnswerJpaEntity>>()) } returns emptyList()

        val response = service.execute(1L, CreateClubApplicationRequest(emptyList()))

        assertEquals(100L, response.applicationId)
    }
}
