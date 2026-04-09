package team.incube.flooding.domain.club.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldType
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubStatus
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.service.impl.GetClubFormServiceImpl
import team.themoment.sdk.exception.ExpectedException

class GetClubFormServiceTest {
    private val clubFormRepository = mockk<ClubFormRepository>()
    private val clubFormFieldRepository = mockk<ClubFormFieldRepository>()
    private val clubFormFieldOptionRepository = mockk<ClubFormFieldOptionRepository>()

    private val service =
        GetClubFormServiceImpl(
            clubFormRepository,
            clubFormFieldRepository,
            clubFormFieldOptionRepository,
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

    private val form = ClubFormJpaEntity(id = 10L, club = club, title = "신청 폼", description = "설명")

    private fun field(
        id: Long,
        order: Int,
        type: ClubFormFieldType = ClubFormFieldType.TEXT,
        required: Boolean = false,
    ) = ClubFormFieldJpaEntity(
        id = id,
        form = form,
        label = "필드$id",
        description = null,
        fieldType = type,
        fieldOrder = order,
        isRequired = required,
    )

    private fun option(
        id: Long,
        field: ClubFormFieldJpaEntity,
        optionOrder: Int,
    ) = ClubFormFieldOptionJpaEntity(
        id = id,
        field = field,
        label = "옵션$id",
        value = "value$id",
        optionOrder = optionOrder,
    )

    @Test
    fun `활성_폼이_없으면_NOT_FOUND_예외가_발생한다`() {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns null

        val exception =
            assertThrows(ExpectedException::class.java) {
                service.execute(1L)
            }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `폼_조회_시_필드가_order_순으로_정렬된다`() {
        val fields = listOf(field(1L, order = 1), field(2L, order = 2), field(3L, order = 3))

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns fields
        every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(any()) } returns emptyList()

        val response = service.execute(1L)

        assertEquals(3, response.fields.size)
        assertEquals(1, response.fields[0].order)
        assertEquals(2, response.fields[1].order)
        assertEquals(3, response.fields[2].order)
    }

    @Test
    fun `라디오_필드에_옵션이_올바르게_조립된다`() {
        val radioField = field(1L, order = 1, type = ClubFormFieldType.RADIO, required = true)
        val options =
            listOf(
                option(1L, radioField, optionOrder = 0),
                option(2L, radioField, optionOrder = 1),
                option(3L, radioField, optionOrder = 2),
            )

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(radioField)
        every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L)) } returns options

        val response = service.execute(1L)

        val fieldResponse = response.fields[0]
        assertEquals(3, fieldResponse.options.size)
        assertEquals("value1", fieldResponse.options[0].value)
        assertEquals("value2", fieldResponse.options[1].value)
        assertEquals("value3", fieldResponse.options[2].value)
    }

    @Test
    fun `텍스트_필드는_옵션이_빈_리스트다`() {
        val textField = field(1L, order = 1, type = ClubFormFieldType.TEXTAREA)

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(textField)
        every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L)) } returns emptyList()

        val response = service.execute(1L)

        assertTrue(response.fields[0].options.isEmpty())
    }

    @Test
    fun `여러_필드의_옵션이_각_필드에_올바르게_매핑된다`() {
        val field1 = field(1L, order = 1, type = ClubFormFieldType.RADIO)
        val field2 = field(2L, order = 2, type = ClubFormFieldType.DROPDOWN)
        val options =
            listOf(
                option(1L, field1, optionOrder = 0),
                option(2L, field1, optionOrder = 1),
                option(3L, field2, optionOrder = 0),
                option(4L, field2, optionOrder = 1),
            )

        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns listOf(field1, field2)
        every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(listOf(1L, 2L)) } returns options

        val response = service.execute(1L)

        assertEquals(2, response.fields[0].options.size)
        assertEquals(2, response.fields[1].options.size)
        assertEquals("value1", response.fields[0].options[0].value)
        assertEquals("value3", response.fields[1].options[0].value)
    }

    @Test
    fun `폼_기본_정보가_올바르게_반환된다`() {
        every { clubFormRepository.findByClubIdAndIsActiveTrue(1L) } returns form
        every { clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(10L) } returns emptyList()
        every { clubFormFieldOptionRepository.findAllByFieldIdInOrderByOptionOrder(emptyList()) } returns emptyList()

        val response = service.execute(1L)

        assertEquals(10L, response.formId)
        assertEquals("신청 폼", response.title)
        assertEquals("설명", response.description)
    }
}
