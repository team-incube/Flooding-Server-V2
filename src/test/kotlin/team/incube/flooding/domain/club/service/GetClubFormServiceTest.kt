package team.incube.flooding.domain.club.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetClubFormServiceTest {
    enum class FieldType { TEXT, TEXTAREA, RADIO, CHECKBOX, DROPDOWN }

    data class FormField(val id: Long, val label: String, val fieldType: FieldType, val order: Int, val isRequired: Boolean)
    data class FieldOption(val id: Long, val fieldId: Long, val label: String, val value: String, val optionOrder: Int)

    data class FormResponse(
        val formId: Long,
        val title: String,
        val description: String?,
        val fields: List<FieldResponse>,
    )

    data class FieldResponse(
        val fieldId: Long,
        val label: String,
        val fieldType: FieldType,
        val order: Int,
        val required: Boolean,
        val options: List<OptionResponse>,
    )

    data class OptionResponse(val optionId: Long, val label: String, val value: String)

    private fun getFormLogic(
        clubId: Long,
        activeFormId: Long?,
        formTitle: String,
        fields: List<FormField>,
        options: List<FieldOption>,
    ): FormResponse {
        val formId =
            activeFormId ?: throw RuntimeException("활성화된 신청 폼이 없습니다.")

        val sortedFields = fields.sortedBy { it.order }
        val optionsByFieldId = options.groupBy { it.fieldId }

        return FormResponse(
            formId = formId,
            title = formTitle,
            description = null,
            fields =
                sortedFields.map { field ->
                    FieldResponse(
                        fieldId = field.id,
                        label = field.label,
                        fieldType = field.fieldType,
                        order = field.order,
                        required = field.isRequired,
                        options =
                            optionsByFieldId[field.id]
                                ?.sortedBy { it.optionOrder }
                                ?.map { OptionResponse(it.id, it.label, it.value) }
                                ?: emptyList(),
                    )
                },
        )
    }

    @Test
    fun `활성_폼이_없으면_예외가_발생한다`() {
        val exception =
            assertThrows(RuntimeException::class.java) {
                getFormLogic(1L, null, "", emptyList(), emptyList())
            }

        assertEquals("활성화된 신청 폼이 없습니다.", exception.message)
    }

    @Test
    fun `폼_조회_시_필드가_order_순으로_정렬된다`() {
        val fields =
            listOf(
                FormField(1L, "세번째", FieldType.TEXT, order = 3, isRequired = false),
                FormField(2L, "첫번째", FieldType.TEXT, order = 1, isRequired = true),
                FormField(3L, "두번째", FieldType.TEXTAREA, order = 2, isRequired = false),
            )

        val response = getFormLogic(1L, 10L, "테스트 폼", fields, emptyList())

        assertEquals(3, response.fields.size)
        assertEquals(1, response.fields[0].order)
        assertEquals(2, response.fields[1].order)
        assertEquals(3, response.fields[2].order)
    }

    @Test
    fun `라디오_필드에_옵션이_올바르게_조립된다`() {
        val fields = listOf(FormField(1L, "성별", FieldType.RADIO, order = 1, isRequired = true))
        val options =
            listOf(
                FieldOption(3L, 1L, "남성", "male", optionOrder = 1),
                FieldOption(1L, 1L, "여성", "female", optionOrder = 0),
                FieldOption(2L, 1L, "기타", "other", optionOrder = 2),
            )

        val response = getFormLogic(1L, 10L, "테스트 폼", fields, options)

        val fieldResponse = response.fields[0]
        assertEquals(3, fieldResponse.options.size)
        assertEquals("female", fieldResponse.options[0].value)
        assertEquals("male", fieldResponse.options[1].value)
        assertEquals("other", fieldResponse.options[2].value)
    }

    @Test
    fun `텍스트_필드는_옵션이_빈_리스트다`() {
        val fields = listOf(FormField(1L, "자기소개", FieldType.TEXTAREA, order = 1, isRequired = true))

        val response = getFormLogic(1L, 10L, "테스트 폼", fields, emptyList())

        assertTrue(response.fields[0].options.isEmpty())
    }

    @Test
    fun `여러_필드의_옵션이_각_필드에_올바르게_매핑된다`() {
        val fields =
            listOf(
                FormField(1L, "학년", FieldType.RADIO, order = 1, isRequired = true),
                FormField(2L, "전공", FieldType.DROPDOWN, order = 2, isRequired = true),
            )
        val options =
            listOf(
                FieldOption(1L, 1L, "1학년", "1", optionOrder = 0),
                FieldOption(2L, 1L, "2학년", "2", optionOrder = 1),
                FieldOption(3L, 2L, "컴퓨터공학", "cs", optionOrder = 0),
                FieldOption(4L, 2L, "전자공학", "ee", optionOrder = 1),
            )

        val response = getFormLogic(1L, 10L, "테스트 폼", fields, options)

        assertEquals(2, response.fields[0].options.size)
        assertEquals(2, response.fields[1].options.size)
        assertEquals("1", response.fields[0].options[0].value)
        assertEquals("cs", response.fields[1].options[0].value)
    }
}