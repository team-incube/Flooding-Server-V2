package team.incube.flooding.domain.dormitory.music.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.presentation.data.request.ApplyWakeUpMusicByUrlRequest
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.impl.ApplyWakeUpMusicServiceImpl
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.client.YoutubeClient
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ApplyWakeUpMusicServiceImplTest :
    BehaviorSpec({
        val wakeUpMusicRepository = mockk<WakeUpMusicRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val youtubeClient = mockk<YoutubeClient>()
        val clock = Clock.fixed(Instant.parse("2026-05-14T01:00:00Z"), ZoneId.of("Asia/Seoul"))

        val service =
            ApplyWakeUpMusicServiceImpl(
                wakeUpMusicRepository = wakeUpMusicRepository,
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

        given("오늘 신청 이력이 없을 때") {
            `when`("execute를 호출하면") {
                then("기상음악이 저장되고 응답이 반환된다") {
                    val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=abc")
                    val fixedNow = LocalDateTime.now(clock)
                    val startOfDay = fixedNow.toLocalDate().atStartOfDay()
                    val endOfDay = startOfDay.plusDays(1)
                    val slot = slot<WakeUpMusicJpaEntity>()

                    every { currentUserProvider.getCurrentUser() } returns user
                    every {
                        wakeUpMusicRepository.existsByUserIdAndAppliedAtBetween(
                            user.id,
                            startOfDay,
                            endOfDay,
                        )
                    } returns
                        false
                    stubYoutubeInfo(request.musicUrl)
                    every { wakeUpMusicRepository.saveAndFlush(capture(slot)) } answers { slot.captured }

                    val result = service.execute(request)

                    verify(exactly = 1) { wakeUpMusicRepository.saveAndFlush(any()) }
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

        given("오늘 이미 신청 이력이 있을 때") {
            `when`("execute를 호출하면") {
                then("CONFLICT 예외가 발생한다") {
                    val request = ApplyWakeUpMusicByUrlRequest("https://youtube.com/watch?v=new")
                    val fixedNow = LocalDateTime.now(clock)
                    val startOfDay = fixedNow.toLocalDate().atStartOfDay()
                    val endOfDay = startOfDay.plusDays(1)

                    every { currentUserProvider.getCurrentUser() } returns user
                    every {
                        wakeUpMusicRepository.existsByUserIdAndAppliedAtBetween(
                            user.id,
                            startOfDay,
                            endOfDay,
                        )
                    } returns
                        true

                    val exception = shouldThrow<ExpectedException> { service.execute(request) }

                    exception.statusCode shouldBe HttpStatus.CONFLICT
                    verify(exactly = 0) { youtubeClient.getVideoInfo(any()) }
                    verify(exactly = 0) { wakeUpMusicRepository.saveAndFlush(any()) }
                }
            }
        }
    })
