package team.incube.flooding.domain.ai.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.incube.flooding.domain.ai.adapter.AiSongAdapter
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.request.SongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.service.impl.RecommendAiSongServiceImpl
import team.incube.flooding.domain.dormitory.music.entity.WakeUpMusicJpaEntity
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class RecommendAiSongServiceImplTest :
    BehaviorSpec({

        val aiSongAdapter = mockk<AiSongAdapter>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val wakeUpMusicRepository = mockk<WakeUpMusicRepository>()
        val service = RecommendAiSongServiceImpl(aiSongAdapter, currentUserProvider, wakeUpMusicRepository)

        val mockUser = mockk<UserJpaEntity>()
        every { mockUser.id } returns 1L
        every { currentUserProvider.getCurrentUser() } returns mockUser

        val mockHistories =
            listOf(
                mockk<WakeUpMusicJpaEntity>().also {
                    every { it.title } returns "Life"
                    every { it.artist } returns "Ignito"
                },
                mockk<WakeUpMusicJpaEntity>().also {
                    every { it.title } returns "Newspaper"
                    every { it.artist } returns "이현준"
                },
                mockk<WakeUpMusicJpaEntity>().also {
                    every { it.title } returns "아침에"
                    every { it.artist } returns "양홍원"
                },
                mockk<WakeUpMusicJpaEntity>().also {
                    every { it.title } returns "Hip Hop"
                    every { it.artist } returns "dwen"
                },
                mockk<WakeUpMusicJpaEntity>().also {
                    every { it.title } returns "우리가 바로"
                    every { it.artist } returns "TEAM NY"
                },
            )

        val expectedRequest =
            RecommendAiSongRequest(
                recentSongs =
                    listOf(
                        SongRequest("Life", "Ignito"),
                        SongRequest("Newspaper", "이현준"),
                        SongRequest("아침에", "양홍원"),
                        SongRequest("Hip Hop", "dwen"),
                        SongRequest("우리가 바로", "TEAM NY"),
                    ),
            )

        Given("AI 음악 추천 서버가 정상 응답할 때") {
            val expectedResponse =
                RecommendAiSongResponse(
                    youtubeLinks =
                        listOf(
                            "https://www.youtube.com/watch?v=abc",
                            "https://www.youtube.com/watch?v=def",
                            "https://www.youtube.com/watch?v=ghi",
                        ),
                )

            every { wakeUpMusicRepository.findTop5ByUserIdOrderByAppliedAtDesc(1L) } returns mockHistories
            every { aiSongAdapter.recommend(expectedRequest) } returns expectedResponse

            When("음악 추천을 요청하면") {
                val response = service.execute()

                Then("유튜브 링크 3개를 반환한다") {
                    response.youtubeLinks.size shouldBe 3
                    response.youtubeLinks[0] shouldBe "https://www.youtube.com/watch?v=abc"
                    response.youtubeLinks[1] shouldBe "https://www.youtube.com/watch?v=def"
                    response.youtubeLinks[2] shouldBe "https://www.youtube.com/watch?v=ghi"
                    verify(exactly = 1) { aiSongAdapter.recommend(expectedRequest) }
                }
            }
        }

        Given("기상송 신청 내역이 없을 때") {
            every { wakeUpMusicRepository.findTop5ByUserIdOrderByAppliedAtDesc(1L) } returns emptyList()

            When("음악 추천을 요청하면") {
                Then("404 예외를 던진다") {
                    val exception = shouldThrow<ExpectedException> { service.execute() }
                    exception.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }
    })
