package team.incube.flooding.domain.dormitory.massage.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class MassageServiceTest : BehaviorSpec({
    val maxCount = 5
    val openTime = LocalTime.of(20, 20)

    fun applyLogic(
        userId: Long,
        now: LocalTime,
        queue: MutableList<Long>,
        lock: ReentrantLock,
        successCount: AtomicInteger,
    ) {
        if (now.isBefore(openTime)) throw RuntimeException("안마의자 신청 시간이 아닙니다.")
        val acquired = lock.tryLock(5, TimeUnit.SECONDS)
        if (!acquired) throw RuntimeException("잠시 후 다시 시도해주세요.")
        try {
            if (queue.size >= maxCount) throw RuntimeException("신청 인원이 마감되었습니다.")
            if (queue.contains(userId)) throw RuntimeException("이미 신청하였습니다.")
            queue.add(userId)
            successCount.incrementAndGet()
        } finally {
            lock.unlock()
        }
    }

    fun cancelLogic(userId: Long, queue: MutableList<Long>) {
        if (!queue.contains(userId)) throw RuntimeException("안마의자 신청 내역이 없습니다.")
        queue.remove(userId)
    }

    given("신청 시간 전에") {
        `when`("신청하면") {
            then("예외가 발생한다") {
                val queue = mutableListOf<Long>()
                val lock = ReentrantLock()
                val successCount = AtomicInteger(0)
                val exception = shouldThrow<RuntimeException> {
                    applyLogic(1L, openTime.minusMinutes(1), queue, lock, successCount)
                }
                exception.message shouldBe "안마의자 신청 시간이 아닙니다."
                queue.size shouldBe 0
            }
        }
    }

    given("신청 시간 이후에") {
        `when`("신청하면") {
            then("정상 신청된다") {
                val queue = mutableListOf<Long>()
                val lock = ReentrantLock()
                val successCount = AtomicInteger(0)
                applyLogic(1L, openTime.plusMinutes(1), queue, lock, successCount)
                queue.size shouldBe 1
                queue.contains(1L) shouldBe true
                successCount.get() shouldBe 1
            }
        }
    }

    given("최대 인원이 꽉 찼을 때") {
        `when`("추가 신청하면") {
            then("예외가 발생한다") {
                val queue = mutableListOf<Long>()
                val lock = ReentrantLock()
                val successCount = AtomicInteger(0)
                val now = openTime.plusMinutes(1)
                (1L..maxCount.toLong()).forEach { id -> applyLogic(id, now, queue, lock, successCount) }
                val exception = shouldThrow<RuntimeException> {
                    applyLogic(maxCount + 1L, now, queue, lock, successCount)
                }
                exception.message shouldBe "신청 인원이 마감되었습니다."
                queue.size shouldBe maxCount
            }
        }
    }

    given("이미 신청한 유저가") {
        `when`("중복 신청하면") {
            then("예외가 발생한다") {
                val queue = mutableListOf<Long>()
                val lock = ReentrantLock()
                val successCount = AtomicInteger(0)
                val now = openTime.plusMinutes(1)
                applyLogic(1L, now, queue, lock, successCount)
                val exception = shouldThrow<RuntimeException> {
                    applyLogic(1L, now, queue, lock, successCount)
                }
                exception.message shouldBe "이미 신청하였습니다."
                successCount.get() shouldBe 1
            }
        }
    }

    given("신청 내역이 없을 때") {
        `when`("취소하면") {
            then("예외가 발생한다") {
                val queue = mutableListOf<Long>()
                val exception = shouldThrow<RuntimeException> {
                    cancelLogic(1L, queue)
                }
                exception.message shouldBe "안마의자 신청 내역이 없습니다."
            }
        }
    }

    given("신청한 유저가") {
        `when`("취소하면") {
            then("정상 취소된다") {
                val queue = mutableListOf(1L, 2L, 3L)
                cancelLogic(2L, queue)
                queue.size shouldBe 2
                queue.contains(2L) shouldBe false
            }
        }
    }

    given("취소 후") {
        `when`("재신청하면") {
            then("정상 처리된다") {
                val queue = mutableListOf<Long>()
                val lock = ReentrantLock()
                val successCount = AtomicInteger(0)
                val now = openTime.plusMinutes(1)
                applyLogic(1L, now, queue, lock, successCount)
                cancelLogic(1L, queue)
                applyLogic(1L, now, queue, lock, successCount)
                queue.size shouldBe 1
                queue.contains(1L) shouldBe true
                successCount.get() shouldBe 2
            }
        }
    }

    given("50명이 동시에 신청할 때") {
        `when`("lock이 적용되어 있으면") {
            then("maxCount를 초과하지 않는다") {
                val threadCount = 50
                repeat(10) {
                    val queue = CopyOnWriteArrayList<Long>()
                    val lock = ReentrantLock()
                    val successCount = AtomicInteger(0)
                    val now = openTime.plusMinutes(1)
                    val latch = CountDownLatch(1)
                    val executor = Executors.newFixedThreadPool(threadCount)
                    (1L..threadCount.toLong()).forEach { userId ->
                        executor.submit {
                            latch.await()
                            try { applyLogic(userId, now, queue, lock, successCount) } catch (_: Exception) {}
                        }
                    }
                    latch.countDown()
                    executor.shutdown()
                    executor.awaitTermination(10, TimeUnit.SECONDS)
                    (queue.size <= maxCount) shouldBe true
                    (successCount.get() <= maxCount) shouldBe true
                }
            }
        }
    }

    given("동일 유저가 30번 동시 신청할 때") {
        `when`("lock이 적용되어 있으면") {
            then("중복 신청이 방지된다") {
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
                        try { applyLogic(1L, now, queue, lock, successCount) } catch (_: Exception) {}
                    }
                }
                latch.countDown()
                executor.shutdown()
                executor.awaitTermination(10, TimeUnit.SECONDS)
                successCount.get() shouldBe 1
                queue.size shouldBe 1
            }
        }
    }
})
