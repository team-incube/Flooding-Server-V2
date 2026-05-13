package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.impl.ApplyWakeUpMusicServiceImpl
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ApplyWakeUpMusicServiceImplTest :
    BehaviorSpec({
        val wakeUpMusicRepository = mockk<WakeUpMusicRepository>()
        val wakeUpMusicLikeRepository = mockk<WakeUpMusicLikeRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val clock =
            Clock.fixed(
                Instant.parse("2026-05-10T14:00:00Z"),
                ZoneId.of("Asia/Seoul"),
            )
        val service =
            ApplyWakeUpMusicServiceImpl(
                wakeUpMusicRepository = wakeUpMusicRepository,
                wakeUpMusicLikeRepository = wakeUpMusicLikeRepository,
                currentUserProvider = currentUserProvider,
                clock = clock,
            )

        val user = mockk<UserJpaEntity>()
        every { user.id } returns 1L
        every { currentUserProvider.getCurrentUser() } returns user

        Given("기상음악을 신청할 때") {
            val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=test")
            val savedMusicSlot = slot<WakeUpMusicJpaEntity>()
            every { wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(1L) } returns emptyList()
            every { wakeUpMusicRepository.save(capture(savedMusicSlot)) } answers { savedMusicSlot.captured }

            When("신청 데이터를 저장하면") {
                val response = service.execute(request)

                Then("신청 시각이 저장된다") {
                    savedMusicSlot.captured.musicUrl shouldBe request.musicUrl
                    savedMusicSlot.captured.appliedAt shouldBe LocalDateTime.of(2026, 5, 10, 23, 0)
                    response.musicUrl shouldBe request.musicUrl
                    response.appliedAt shouldBe LocalDateTime.of(2026, 5, 10, 23, 0)
                    verify(exactly = 1) { wakeUpMusicRepository.save(any()) }
                }
            }
        }
    })
