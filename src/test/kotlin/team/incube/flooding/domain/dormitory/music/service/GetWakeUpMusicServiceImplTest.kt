package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
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

        beforeEach { clearAllMocks() }

        given("날짜가 2026-05-14일 때") {
            `when`("execute를 호출하면") {
                then("해당 날짜의 00:00 ~ 익일 00:00 범위로 조회한다") {
                    val date = LocalDate.of(2026, 5, 14)
                    val expectedStart = LocalDateTime.of(2026, 5, 14, 0, 0)
                    val expectedEnd = LocalDateTime.of(2026, 5, 15, 0, 0)
                    val mockResponse =
                        listOf(
                            WakeUpMusicResponse(
                                id = 1L,
                                musicUrl = "https://youtube.com/watch?v=abc",
                                appliedAt = LocalDateTime.of(2026, 5, 14, 10, 0),
                                likeCount = 3,
                            ),
                        )

                    every { wakeUpMusicRepository.findAllWithLikeCountByDate(expectedStart, expectedEnd) } returns
                        mockResponse

                    val result = service.execute(date)

                    verify(exactly = 1) {
                        wakeUpMusicRepository.findAllWithLikeCountByDate(expectedStart, expectedEnd)
                    }
                    result shouldBe mockResponse
                }
            }
        }

        given("해당 날짜에 신청된 기상음악이 없을 때") {
            `when`("execute를 호출하면") {
                then("빈 리스트가 반환된다") {
                    val date = LocalDate.of(2026, 5, 14)
                    val expectedStart = LocalDateTime.of(2026, 5, 14, 0, 0)
                    val expectedEnd = LocalDateTime.of(2026, 5, 15, 0, 0)

                    every { wakeUpMusicRepository.findAllWithLikeCountByDate(expectedStart, expectedEnd) } returns
                        emptyList()

                    val result = service.execute(date)

                    result shouldBe emptyList()
                }
            }
        }
    })
