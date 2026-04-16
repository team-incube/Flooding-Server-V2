package team.incube.flooding.domain.dormitory.study.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * StudyApplicationServiceImpl 동시성 문제 재현 및 수정안 검증 테스트
 *
 * Redis 개별 명령은 원자적이지만, 여러 명령의 조합은 원자적이지 않다.
 * 현재 구현의 "check-then-act" 패턴이 레이스 컨디션을 유발한다.
 */
class StudyApplicationConcurrencyTest {
    private val maxCount = 5
    private val threadCount = 50

    /**
     * 현재 구현 시뮬레이션:
     * getCount() 체크 → saveApplication() → incrementCount() 순서로 실행
     * getCount()와 incrementCount() 사이가 비원자적 → count 초과 가능
     */
    private fun buggyApply(
        userId: Long,
        count: AtomicInteger,
        statusMap: ConcurrentHashMap<Long, StudyApplicationStatus>,
        successCount: AtomicInteger,
    ) {
        val status = statusMap[userId]
        if (status == StudyApplicationStatus.BANNED) throw RuntimeException("banned")
        if (status == StudyApplicationStatus.APPROVED || status == StudyApplicationStatus.CANCELLED) {
            throw RuntimeException("이미 신청함")
        }

        if (count.get() >= maxCount) throw RuntimeException("마감")

        // count 확인 후 save/increment 사이에 다른 스레드가 통과할 수 있음
        statusMap[userId] = StudyApplicationStatus.APPROVED
        count.incrementAndGet()
        successCount.incrementAndGet()
    }

    /**
     * 수정안 1: incrementCount 먼저 수행 후 초과 시 롤백
     * Redis INCR은 원자적이므로 increment 결과값으로 판단 → 오버슈트 없음
     */
    private fun fixedApply(
        userId: Long,
        count: AtomicInteger,
        statusMap: ConcurrentHashMap<Long, StudyApplicationStatus>,
        successCount: AtomicInteger,
    ) {
        val status = statusMap[userId]
        if (status == StudyApplicationStatus.BANNED) throw RuntimeException("banned")
        if (status == StudyApplicationStatus.APPROVED || status == StudyApplicationStatus.CANCELLED) {
            throw RuntimeException("이미 신청함")
        }

        val newCount = count.incrementAndGet()
        if (newCount > maxCount) {
            count.decrementAndGet()
            throw RuntimeException("마감")
        }

        // putIfAbsent: SET NX 동작 → 중복 유저 신청도 방지
        val prev = statusMap.putIfAbsent(userId, StudyApplicationStatus.APPROVED)
        if (prev != null) {
            count.decrementAndGet()
            throw RuntimeException("이미 신청함 (동시 요청)")
        }

        successCount.incrementAndGet()
    }

    @Disabled("타이밍 의존적인 flaky 테스트 - 로컬에서만 수동 실행")
    @Test
    fun `현재구현_동시요청시_maxCount를_초과할_수_있다`() {
        var bugged = false

        repeat(20) {
            val count = AtomicInteger(0)
            val statusMap = ConcurrentHashMap<Long, StudyApplicationStatus>()
            val successCount = AtomicInteger(0)
            val latch = CountDownLatch(1)
            val executor = Executors.newFixedThreadPool(threadCount)

            (1L..threadCount.toLong()).forEach { userId ->
                executor.submit {
                    latch.await()
                    try {
                        buggyApply(userId, count, statusMap, successCount)
                    } catch (_: Exception) {
                    }
                }
            }

            latch.countDown()
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)

            if (count.get() > maxCount) {
                println("[버그 재현] 시도 ${it + 1}: count=${count.get()}, 성공=${successCount.get()}, MAX=$maxCount")
                bugged = true
                return@repeat
            }
        }

        // 재현에 실패해도 경고만 출력 (타이밍 의존적인 테스트의 한계)
        if (!bugged) println("[경고] 20회 시도에서 버그가 재현되지 않았습니다. 테스트 환경에 따라 다를 수 있습니다.")
        assertTrue(bugged, "동시성 버그가 재현되어야 합니다. threadCount를 늘려서 재시도하세요.")
    }

    @Test
    fun `수정안_incrementFirst_방식은_maxCount를_절대_초과하지_않는다`() {
        repeat(20) {
            val count = AtomicInteger(0)
            val statusMap = ConcurrentHashMap<Long, StudyApplicationStatus>()
            val successCount = AtomicInteger(0)
            val latch = CountDownLatch(1)
            val executor = Executors.newFixedThreadPool(threadCount)

            (1L..threadCount.toLong()).forEach { userId ->
                executor.submit {
                    latch.await()
                    try {
                        fixedApply(userId, count, statusMap, successCount)
                    } catch (_: Exception) {
                    }
                }
            }

            latch.countDown()
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)

            println("[수정 후] 시도 ${it + 1}: count=${count.get()}, 성공=${successCount.get()}, MAX=$maxCount")
            assertTrue(count.get() <= maxCount, "count=${count.get()} > maxCount=$maxCount")
            assertTrue(successCount.get() <= maxCount, "successCount=${successCount.get()} > maxCount=$maxCount")
        }
    }

    @Test
    fun `수정안_동일유저_동시요청시_중복신청이_방지된다`() {
        val sameUserId = 1L
        val concurrentRequests = 20

        val count = AtomicInteger(0)
        val statusMap = ConcurrentHashMap<Long, StudyApplicationStatus>()
        val successCount = AtomicInteger(0)
        val latch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(concurrentRequests)

        repeat(concurrentRequests) {
            executor.submit {
                latch.await()
                try {
                    fixedApply(sameUserId, count, statusMap, successCount)
                } catch (_: Exception) {
                }
            }
        }

        latch.countDown()
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        println("[중복 방지] 성공 수: ${successCount.get()}, count: ${count.get()}")
        assertTrue(successCount.get() == 1, "동일 유저는 1번만 성공해야 합니다. 실제: ${successCount.get()}")
        assertTrue(count.get() == 1, "count는 1이어야 합니다. 실제: ${count.get()}")
    }
}
