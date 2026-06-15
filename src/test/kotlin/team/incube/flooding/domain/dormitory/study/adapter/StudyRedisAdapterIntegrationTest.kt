package team.incube.flooding.domain.dormitory.study.adapter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.support.IntegrationTestBase
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudyRedisAdapterIntegrationTest : IntegrationTestBase() {
    @Autowired
    lateinit var studyRedisAdapter: StudyRedisAdapter

    @BeforeEach
    fun setUp() {
        studyRedisAdapter.resetAll()
    }

    @Test
    fun `saveApplication 후 getApplicationStatus로 조회할 수 있다`() {
        studyRedisAdapter.saveApplication(1L, StudyApplicationStatus.APPROVED)

        val result = studyRedisAdapter.getApplicationStatus(1L)

        assertEquals(StudyApplicationStatus.APPROVED, result)
    }

    @Test
    fun `저장되지 않은 userId는 getApplicationStatus가 null을 반환한다`() {
        val result = studyRedisAdapter.getApplicationStatus(999L)

        assertNull(result)
    }

    @Test
    fun `incrementCount는 카운트를 원자적으로 증가시킨다`() {
        val first = studyRedisAdapter.incrementCount()
        val second = studyRedisAdapter.incrementCount()

        assertEquals(1L, first)
        assertEquals(2L, second)
        assertEquals(2L, studyRedisAdapter.getCount())
    }

    @Test
    fun `decrementCount는 카운트를 감소시킨다`() {
        studyRedisAdapter.incrementCount()
        studyRedisAdapter.incrementCount()
        studyRedisAdapter.decrementCount()

        assertEquals(1L, studyRedisAdapter.getCount())
    }

    @Test
    fun `카운트가 없을 때 getCount는 0을 반환한다`() {
        assertEquals(0L, studyRedisAdapter.getCount())
    }

    @Test
    fun `addApplicant 후 getApplicantIds에 포함된다`() {
        studyRedisAdapter.addApplicant(1L)
        studyRedisAdapter.addApplicant(2L)

        val ids = studyRedisAdapter.getApplicantIds()

        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun `resetAll 후 모든 데이터가 초기화된다`() {
        studyRedisAdapter.saveApplication(1L, StudyApplicationStatus.APPROVED)
        studyRedisAdapter.incrementCount()
        studyRedisAdapter.addApplicant(1L)

        studyRedisAdapter.resetAll()

        assertNull(studyRedisAdapter.getApplicationStatus(1L))
        assertEquals(0L, studyRedisAdapter.getCount())
        assertTrue(studyRedisAdapter.getApplicantIds().isEmpty())
    }
}
