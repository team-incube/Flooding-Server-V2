package team.incube.flooding.domain.dormitory.massage.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
 * 마사지 신청/취소 서비스 로직 테스트
 *
 * ApplyMassageServiceImpl, CancelMassageServiceImpl의 핵심 로직을
 * 인메모리로 시뮬레이션하여 검증합니다.
 */
class MassageServiceTest {
    private val maxCount = 5
    private val openTime = LocalTime.of(20, 20)

    // ApplyMassageServiceImpl 핵심 로직 시뮬레이션
    private fun applyLogic(
        userId: Long,
        now: LocalTime,
        queue: MutableList<Long>,
        lock: ReentrantLock,
        successCount: AtomicInteger,
    ) {
        if (now.isBefore(openTime)) {
            throw RuntimeException("안마의자 신청 시간이 아닙니다.")
        }

        val acquired = lock.tryLock(5, TimeUnit.SECONDS)
        if (!acquired) {
            throw RuntimeException("잠시 후 다시 시도해주세요.")
        }

        try {
            if (queue.size >= maxCount) {
                throw RuntimeException("신청 인원이 마감되었습니다.")
            }
            if (queue.contains(userId)) {
                throw RuntimeException("이미 신청하였습니다.")
            }
            queue.add(userId)
            successCount.incrementAndGet()
        } finally {
            lock.unlock()
        }
    }

    // CancelMassageServiceImpl 핵심 로직 시뮬레이션
    private fun cancelLogic(
        userId: Long,
        queue: MutableList<Long>,
    ) {
        if (!queue.contains(userId)) {
            throw RuntimeException("안마의자 신청 내역이 없습니다.")
        }
        queue.remove(userId)
    }

    // ── Apply 단위 테스트 ──────────────────────────────────────────────────────

    @Test
    fun `신청_시간_전에는_예외가_발생한다`() {
        val queue = mutableListOf<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val beforeOpenTime = openTime.minusMinutes(1)

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, beforeOpenTime, queue, lock, successCount)
            }

        assertEquals("안마의자 신청 시간이 아닙니다.", exception.message)
        assertEquals(0, queue.size)
    }

    @Test
    fun `신청_시간_이후에는_정상_신청된다`() {
        val queue = mutableListOf<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val afterOpenTime = openTime.plusMinutes(1)

        applyLogic(1L, afterOpenTime, queue, lock, successCount)

        assertEquals(1, queue.size)
        assertTrue(queue.contains(1L))
        assertEquals(1, successCount.get())
    }

    @Test
    fun `최대_인원_초과_시_예외가_발생한다`() {
        val queue = mutableListOf<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val now = openTime.plusMinutes(1)

        // maxCount만큼 먼저 채움
        (1L..maxCount.toLong()).forEach { id ->
            applyLogic(id, now, queue, lock, successCount)
        }
        assertEquals(maxCount, queue.size)

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(maxCount + 1L, now, queue, lock, successCount)
            }

        assertEquals("신청 인원이 마감되었습니다.", exception.message)
        assertEquals(maxCount, queue.size)
    }

    @Test
    fun `동일_유저_중복_신청_시_예외가_발생한다`() {
        val queue = mutableListOf<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val now = openTime.plusMinutes(1)

        applyLogic(1L, now, queue, lock, successCount)

        val exception =
            assertThrows(RuntimeException::class.java) {
                applyLogic(1L, now, queue, lock, successCount)
            }

        assertEquals("이미 신청하였습니다.", exception.message)
        assertEquals(1, successCount.get())
    }

    // ── Cancel 단위 테스트 ─────────────────────────────────────────────────────

    @Test
    fun `신청_내역이_없을_때_취소하면_예외가_발생한다`() {
        val queue = mutableListOf<Long>()

        val exception =
            assertThrows(RuntimeException::class.java) {
                cancelLogic(1L, queue)
            }

        assertEquals("안마의자 신청 내역이 없습니다.", exception.message)
    }

    @Test
    fun `신청한_유저는_정상_취소된다`() {
        val queue = mutableListOf(1L, 2L, 3L)

        cancelLogic(2L, queue)

        assertEquals(2, queue.size)
        assertFalse(queue.contains(2L))
    }

    @Test
    fun `취소_후_재신청이_가능하다`() {
        val queue = mutableListOf<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val now = openTime.plusMinutes(1)

        applyLogic(1L, now, queue, lock, successCount)
        cancelLogic(1L, queue)
        applyLogic(1L, now, queue, lock, successCount)

        assertEquals(1, queue.size)
        assertTrue(queue.contains(1L))
        assertEquals(2, successCount.get())
    }

    // ── 동시성 테스트 ──────────────────────────────────────────────────────────

    @Test
    fun `동시_요청에서_lock으로_인해_maxCount를_초과하지_않는다`() {
        val threadCount = 50

        repeat(10) { attempt ->
            val queue = CopyOnWriteArrayList<Long>()
            val lock = ReentrantLock()
            val successCount = AtomicInteger(0)
            val now = openTime.plusMinutes(1)
            val latch = CountDownLatch(1)
            val executor = Executors.newFixedThreadPool(threadCount)

            (1L..threadCount.toLong()).forEach { userId ->
                executor.submit {
                    latch.await()
                    try {
                        applyLogic(userId, now, queue, lock, successCount)
                    } catch (_: Exception) {
                    }
                }
            }

            latch.countDown()
            executor.shutdown()
            executor.awaitTermination(10, TimeUnit.SECONDS)

            println("[동시성] 시도 ${attempt + 1}: queue=${queue.size}, 성공=${successCount.get()}, MAX=$maxCount")
            assertTrue(queue.size <= maxCount, "queue.size=${queue.size} > maxCount=$maxCount")
            assertTrue(successCount.get() <= maxCount, "successCount=${successCount.get()} > maxCount=$maxCount")
        }
    }

    @Test
    fun `동일_유저_동시_요청에서_중복_신청이_방지된다`() {
        val sameUserId = 1L
        val concurrentRequests = 30

        val queue = CopyOnWriteArrayList<Long>()
        val lock = ReentrantLock()
        val successCount = AtomicInteger(0)
        val now = openTime.plusMinutes(1)
        val latch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(concurrentRequests)

        repeat(concurrentRequests) {
            executor.submit {
                latch.await()
                try {
                    applyLogic(sameUserId, now, queue, lock, successCount)
                } catch (_: Exception) {
                }
            }
        }

        latch.countDown()
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        println("[중복 방지] 성공 수: ${successCount.get()}, queue: ${queue.size}")
        assertEquals(1, successCount.get(), "동일 유저는 1번만 성공해야 합니다. 실제: ${successCount.get()}")
        assertEquals(1, queue.size, "queue 크기는 1이어야 합니다. 실제: ${queue.size}")
    }
}
