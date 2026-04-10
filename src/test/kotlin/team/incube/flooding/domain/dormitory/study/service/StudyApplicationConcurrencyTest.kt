package team.incube.flooding.domain.dormitory.study.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class StudyApplicationConcurrencyTest : BehaviorSpec({
    val maxCount = 5
    val threadCount = 50

    fun buggyApply(
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
        statusMap[userId] = StudyApplicationStatus.APPROVED
        count.incrementAndGet()
        successCount.incrementAndGet()
    }

    fun fixedApply(
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
        val prev = statusMap.putIfAbsent(userId, StudyApplicationStatus.APPROVED)
        if (prev != null) {
            count.decrementAndGet()
            throw RuntimeException("이미 신청함 (동시 요청)")
        }
        successCount.incrementAndGet()
    }

    given("incrementFirst 방식으로 동시 신청할 때") {
        `when`("50명이 동시에 신청하면") {
            then("maxCount를 초과하지 않는다") {
                repeat(20) {
                    val count = AtomicInteger(0)
                    val statusMap = ConcurrentHashMap<Long, StudyApplicationStatus>()
                    val successCount = AtomicInteger(0)
                    val latch = CountDownLatch(1)
                    val executor = Executors.newFixedThreadPool(threadCount)
                    (1L..threadCount.toLong()).forEach { userId ->
                        executor.submit {
                            latch.await()
                            try { fixedApply(userId, count, statusMap, successCount) } catch (_: Exception) {}
                        }
                    }
                    latch.countDown()
                    executor.shutdown()
                    executor.awaitTermination(5, TimeUnit.SECONDS)
                    (count.get() <= maxCount) shouldBe true
                    (successCount.get() <= maxCount) shouldBe true
                }
            }
        }
    }

    given("incrementFirst 방식으로 동일 유저가 동시 신청할 때") {
        `when`("20번 동시 요청하면") {
            then("중복 신청이 방지된다") {
                val concurrentRequests = 20
                val count = AtomicInteger(0)
                val statusMap = ConcurrentHashMap<Long, StudyApplicationStatus>()
                val successCount = AtomicInteger(0)
                val latch = CountDownLatch(1)
                val executor = Executors.newFixedThreadPool(concurrentRequests)
                repeat(concurrentRequests) {
                    executor.submit {
                        latch.await()
                        try { fixedApply(1L, count, statusMap, successCount) } catch (_: Exception) {}
                    }
                }
                latch.countDown()
                executor.shutdown()
                executor.awaitTermination(5, TimeUnit.SECONDS)
                successCount.get() shouldBe 1
                count.get() shouldBe 1
            }
        }
    }
})
