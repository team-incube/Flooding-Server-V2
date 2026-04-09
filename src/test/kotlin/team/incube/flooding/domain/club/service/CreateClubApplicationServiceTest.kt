package team.incube.flooding.domain.club.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateClubApplicationServiceTest {
    data class Field(val id: Long, val isRequired: Boolean)
    data class Submission(val formId: Long, val userId: Long)

    private val submissions = mutableListOf<Submission>()
    private val answers = mutableMapOf<Long, MutableList<Pair<Long, String?>>>()
    private var submissionIdSeq = 0L

    private val fields =
        listOf(
            Field(1L, isRequired = true),
            Field(2L, isRequired = false),
            Field(3L, isRequired = true),
        )

    @BeforeEach
    fun setUp() {
        submissions.clear()
        answers.clear()
        submissionIdSeq = 0L
    }

    private fun applyLogic(
        formId: Long,
        userId: Long,
        isActiveForm: Boolean,
        answerMap: Map<Long, String?>,
    ): Long {
        if (!isActiveForm) {
            throw RuntimeException("활성화된 신청 폼이 없습니다.")
        }

        if (submissions.any { it.formId == formId && it.userId == userId }) {
            throw RuntimeException("이미 신청한 동아리입니다.")
        }

        fields.filter { it.isRequired }.forEach { field ->
            val value = answerMap[field.id]
            if (value.isNullOrBlank()) {
                throw RuntimeException("'필드${field.id}' 항목은 필수입니다.")
            }
        }

        val id = ++submissionIdSeq
        submissions.add(Submission(formId, userId))
        answers[id] = answerMap.entries.map { Pair(it.key, it.value) }.toMutableList()
        return id
    }

    @Test
    fun `활성_폼이_없으면_예외가_발생한다`() {
        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, 1L, isActiveForm = false, answerMap = emptyMap())
            }

        assertEquals("활성화된 신청 폼이 없습니다.", exception.message)
        assertEquals(0, submissions.size)
    }

    @Test
    fun `중복_신청_시_예외가_발생한다`() {
        val validAnswers = mapOf(1L to "답변1", 2L to null, 3L to "답변3")

        applyLogic(1L, 1L, isActiveForm = true, answerMap = validAnswers)

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, 1L, isActiveForm = true, answerMap = validAnswers)
            }

        assertEquals("이미 신청한 동아리입니다.", exception.message)
        assertEquals(1, submissions.size)
    }

    @Test
    fun `필수_항목_미입력_시_예외가_발생한다`() {
        val missingRequired = mapOf(1L to "답변1", 2L to null, 3L to "")

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, 1L, isActiveForm = true, answerMap = missingRequired)
            }

        assertEquals("'필드3' 항목은 필수입니다.", exception.message)
        assertEquals(0, submissions.size)
    }

    @Test
    fun `필수_항목_미포함_시_예외가_발생한다`() {
        val noRequired = mapOf(2L to "선택 답변")

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, 1L, isActiveForm = true, answerMap = noRequired)
            }

        assertEquals("'필드1' 항목은 필수입니다.", exception.message)
    }

    @Test
    fun `정상_신청_시_submissionId가_반환된다`() {
        val validAnswers = mapOf(1L to "답변1", 2L to null, 3L to "답변3")

        val submissionId = applyLogic(1L, 1L, isActiveForm = true, answerMap = validAnswers)

        assertEquals(1L, submissionId)
        assertEquals(1, submissions.size)
    }

    @Test
    fun `서로_다른_유저는_같은_폼에_각각_신청할_수_있다`() {
        val answers1 = mapOf(1L to "유저1 답변1", 3L to "유저1 답변3")
        val answers2 = mapOf(1L to "유저2 답변1", 3L to "유저2 답변3")

        val id1 = applyLogic(1L, 1L, isActiveForm = true, answerMap = answers1)
        val id2 = applyLogic(1L, 2L, isActiveForm = true, answerMap = answers2)

        assertEquals(2, submissions.size)
        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }

    @Test
    fun `선택_항목은_값이_없어도_신청_가능하다`() {
        val onlyRequired = mapOf(1L to "답변1", 3L to "답변3")

        val submissionId = applyLogic(1L, 1L, isActiveForm = true, answerMap = onlyRequired)

        assertEquals(1L, submissionId)
    }
}