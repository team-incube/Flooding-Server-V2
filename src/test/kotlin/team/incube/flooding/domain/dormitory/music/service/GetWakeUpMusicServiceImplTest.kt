package team.incube.flooding.domain.dormitory.music.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.flooding.domain.dormitory.music.presentation.data.response.WakeUpMusicResponse
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicLikeRepository
import team.incube.flooding.domain.dormitory.music.repository.WakeUpMusicRepository
import team.incube.flooding.domain.dormitory.music.service.impl.GetWakeUpMusicServiceImpl
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import java.time.LocalDate
import java.time.LocalDateTime

class GetWakeUpMusicServiceImplTest :
    BehaviorSpec({
        val wakeUpMusicRepository = mockk<WakeUpMusicRepository>()
        val wakeUpMusicLikeRepository = mockk<WakeUpMusicLikeRepository>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service = GetWakeUpMusicServiceImpl(wakeUpMusicRepository, wakeUpMusicLikeRepository, currentUserProvider)

        val mockUser = mockk<UserJpaEntity>(relaxed = true)

        beforeEach {
            clearAllMocks()
            every { mockUser.id } returns 1L
            every { currentUserProvider.getCurrentUser() } returns mockUser
        }

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
                                isLiked = false,
                            ),
                        )

                    every { wakeUpMusicRepository.findAllWithLikeCountByDate(expectedStart, expectedEnd) } returns
                        mockResponse
                    every { wakeUpMusicLikeRepository.findLikedMusicIdsByUserIdAndMusicIdIn(1L, listOf(1L)) } returns
                        emptyList()

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

        given("현재 유저가 좋아요를 누른 기상음악이 있을 때") {
            `when`("execute를 호출하면") {
                then("isLiked가 true인 항목이 포함된다") {
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
                                isLiked = false,
                            ),
                            WakeUpMusicResponse(
                                id = 2L,
                                musicUrl = "https://youtube.com/watch?v=def",
                                appliedAt = LocalDateTime.of(2026, 5, 14, 11, 0),
                                likeCount = 1,
                                isLiked = false,
                            ),
                        )

                    every { wakeUpMusicRepository.findAllWithLikeCountByDate(expectedStart, expectedEnd) } returns
                        mockResponse
                    every {
                        wakeUpMusicLikeRepository.findLikedMusicIdsByUserIdAndMusicIdIn(1L, listOf(1L, 2L))
                    } returns listOf(2L)

                    val result = service.execute(date)

                    result[0].isLiked shouldBe false
                    result[1].isLiked shouldBe true
                }
            }
        }
    })
