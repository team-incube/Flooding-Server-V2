package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.impl.GetWakeUpMusicServiceImpl
import java.time.LocalDate
import java.time.LocalDateTime

class GetWakeUpMusicServiceImplTest :
    BehaviorSpec({
        val wakeUpMusicRepository = mockk<WakeUpMusicRepository>()
        val service = GetWakeUpMusicServiceImpl(wakeUpMusicRepository)

        Given("특정 날짜의 기상음악을 조회할 때") {
            val targetDate = LocalDate.of(2026, 5, 11)
            val expectedResponse =
                listOf(
                    WakeUpMusicResponse(
                        id = 1L,
                        musicUrl = "https://youtube.com/watch?v=test",
                        appliedAt = LocalDateTime.of(2026, 5, 11, 0, 0),
                        likeCount = 2,
                    ),
                )
            every {
                wakeUpMusicRepository.findAllWithLikeCountByDate(
                    LocalDateTime.of(2026, 5, 11, 0, 0),
                    LocalDateTime.of(2026, 5, 12, 0, 0),
                )
            } returns expectedResponse

            When("서비스가 목록을 조회하면") {
                val response = service.execute(targetDate)

                Then("조회 날짜의 자정 범위를 사용한다") {
                    response shouldBe expectedResponse
                    verify(exactly = 1) {
                        wakeUpMusicRepository.findAllWithLikeCountByDate(
                            LocalDateTime.of(2026, 5, 11, 0, 0),
                            LocalDateTime.of(2026, 5, 12, 0, 0),
                        )
                    }
                }
            }
        }
    })
