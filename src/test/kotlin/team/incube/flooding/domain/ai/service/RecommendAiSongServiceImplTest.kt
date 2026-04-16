package team.incube.flooding.domain.ai.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.ai.adapter.AiSongAdapter
import team.incube.flooding.domain.ai.presentation.data.request.RecommendAiSongRequest
import team.incube.flooding.domain.ai.presentation.data.request.SongRequest
import team.incube.flooding.domain.ai.presentation.data.response.RecommendAiSongResponse
import team.incube.flooding.domain.ai.service.impl.RecommendAiSongServiceImpl

class RecommendAiSongServiceImplTest :
    BehaviorSpec({

        val aiSongAdapter = mockk<AiSongAdapter>()
        val service = RecommendAiSongServiceImpl(aiSongAdapter)

        val sampleRequest =
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
            every { aiSongAdapter.recommend(sampleRequest) } returns expectedResponse

            When("음악 추천을 요청하면") {
                val response = service.execute(sampleRequest)

                Then("유튜브 링크 3개를 반환한다") {
                    response.youtubeLinks.size shouldBe 3
                    response.youtubeLinks[0] shouldBe "https://www.youtube.com/watch?v=abc"
                    response.youtubeLinks[1] shouldBe "https://www.youtube.com/watch?v=def"
                    response.youtubeLinks[2] shouldBe "https://www.youtube.com/watch?v=ghi"
                    verify(exactly = 1) { aiSongAdapter.recommend(sampleRequest) }
                }
            }
        }

        Given("AI 음악 추천 서버가 빈 링크를 반환할 때") {
            val emptyResponse = RecommendAiSongResponse(youtubeLinks = emptyList())
            every { aiSongAdapter.recommend(sampleRequest) } returns emptyResponse

            When("음악 추천을 요청하면") {
                val response = service.execute(sampleRequest)

                Then("빈 리스트를 반환한다") {
                    response.youtubeLinks.size shouldBe 0
                }
            }
        }
    })
