package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.impl.ApplyWakeUpMusicServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.client.YoutubeClient
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
        val youtubeClient = mockk<YoutubeClient>()
        val clock = Clock.fixed(Instant.parse("2026-05-14T01:00:00Z"), ZoneId.of("Asia/Seoul"))

        val service =
            ApplyWakeUpMusicServiceImpl(
                wakeUpMusicRepository = wakeUpMusicRepository,
                wakeUpMusicLikeRepository = wakeUpMusicLikeRepository,
                currentUserProvider = currentUserProvider,
                youtubeClient = youtubeClient,
                clock = clock,
            )

        val user =
            UserJpaEntity(
                id = 1L,
                name = "테스트",
                sex = Sex.MAN,
                email = "test@gsm.hs.kr",
                studentNumber = 1101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
            )

        beforeEach { clearAllMocks() }

        fun stubYoutubeInfo(url: String) {
            every { youtubeClient.getVideoInfo(url) } returns
                YoutubeClient.VideoInfo(
                    title = "테스트 음악",
                    artist = "테스트 채널",
                    duration = "PT3M21S",
                    durationText = "3:21",
                    thumbnailUrl = "https://img.youtube.com/vi/test/maxresdefault.jpg",
                    videoUrl = "https://www.youtube.com/watch?v=test",
                )
        }

        given("신청 이력이 없을 때") {
            `when`("execute를 호출하면") {
                then("기상음악이 저장되고 응답이 반환된다") {
                    val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=abc")
                    val fixedNow = LocalDateTime.now(clock)
                    val slot = slot<WakeUpMusicJpaEntity>()

                    every { currentUserProvider.getCurrentUser() } returns user
                    every { wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(user.id) } returns emptyList()
                    stubYoutubeInfo(request.musicUrl)
                    every { wakeUpMusicRepository.save(capture(slot)) } answers { slot.captured }

                    val result = service.execute(request)

                    verify(exactly = 1) { wakeUpMusicRepository.save(any()) }
                    slot.captured.musicUrl shouldBe "https://www.youtube.com/watch?v=test"
                    slot.captured.title shouldBe "테스트 음악"
                    slot.captured.artist shouldBe "테스트 채널"
                    slot.captured.durationText shouldBe "3:21"
                    slot.captured.thumbnailUrl shouldBe "https://img.youtube.com/vi/test/maxresdefault.jpg"
                    slot.captured.videoUrl shouldBe "https://www.youtube.com/watch?v=test"
                    slot.captured.appliedAt shouldBe fixedNow
                    result.musicUrl shouldBe "https://www.youtube.com/watch?v=test"
                    result.title shouldBe "테스트 음악"
                    result.likeCount shouldBe 0
                }
            }
        }

        given("신청 이력이 정확히 5개일 때") {
            `when`("execute를 호출하면") {
                then("가장 오래된 이력 1개가 삭제된 후 저장된다") {
                    val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=new")
                    val histories =
                        (1L..5L).map { id ->
                            WakeUpMusicJpaEntity(
                                id = id,
                                user = user,
                                musicUrl = "https://youtube.com/watch?v=$id",
                                appliedAt = LocalDateTime.now(clock).minusDays(6 - id),
                            )
                        }
                    val slot = slot<WakeUpMusicJpaEntity>()

                    every { currentUserProvider.getCurrentUser() } returns user
                    every { wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(user.id) } returns histories
                    every { wakeUpMusicLikeRepository.deleteAllByMusicIdIn(listOf(1L)) } returns Unit
                    every { wakeUpMusicRepository.deleteAllInBatch(listOf(histories[0])) } returns Unit
                    stubYoutubeInfo(request.musicUrl)
                    every { wakeUpMusicRepository.save(capture(slot)) } answers { slot.captured }

                    service.execute(request)

                    verify(exactly = 1) { wakeUpMusicLikeRepository.deleteAllByMusicIdIn(listOf(1L)) }
                    verify(exactly = 1) { wakeUpMusicRepository.deleteAllInBatch(listOf(histories[0])) }
                    verify(exactly = 1) { wakeUpMusicRepository.save(any()) }
                }
            }
        }

        given("신청 이력이 4개일 때") {
            `when`("execute를 호출하면") {
                then("삭제 없이 바로 저장된다") {
                    val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=new")
                    val histories =
                        (1L..4L).map { id ->
                            WakeUpMusicJpaEntity(
                                id = id,
                                user = user,
                                musicUrl = "https://youtube.com/watch?v=$id",
                                appliedAt = LocalDateTime.now(clock).minusDays(5 - id),
                            )
                        }
                    val slot = slot<WakeUpMusicJpaEntity>()

                    every { currentUserProvider.getCurrentUser() } returns user
                    every { wakeUpMusicRepository.findAllByUserIdOrderByAppliedAtAsc(user.id) } returns histories
                    stubYoutubeInfo(request.musicUrl)
                    every { wakeUpMusicRepository.save(capture(slot)) } answers { slot.captured }

                    service.execute(request)

                    verify(exactly = 0) { wakeUpMusicLikeRepository.deleteAllByMusicIdIn(any()) }
                    verify(exactly = 0) { wakeUpMusicRepository.deleteAllInBatch(any()) }
                    verify(exactly = 1) { wakeUpMusicRepository.save(any()) }
                }
            }
        }
    })
