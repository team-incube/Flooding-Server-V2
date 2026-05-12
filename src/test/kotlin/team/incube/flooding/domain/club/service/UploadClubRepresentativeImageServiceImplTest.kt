package team.incube.flooding.domain.club.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.domain.club.service.impl.UploadClubRepresentativeImageServiceImpl
import team.incube.flooding.global.util.FileStorageService

class UploadClubRepresentativeImageServiceImplTest :
    BehaviorSpec({
        val fileStorageService = mockk<FileStorageService>()
        val service = UploadClubRepresentativeImageServiceImpl(fileStorageService)

        beforeEach { clearAllMocks() }

        fun image() = MockMultipartFile("image", "representative.png", "image/png", byteArrayOf(1, 2, 3))

        given("동아리 대표 이미지 업로드 요청이 들어오면") {
            `when`("이미지를 저장한다") {
                then("저장된 이미지 URL을 반환한다") {
                    every { fileStorageService.store(any(), "clubs") } returns
                        "https://dev-api.example.com/images/clubs/representative.png"

                    val response = service.execute(image())

                    response.imageUrl shouldBe "https://dev-api.example.com/images/clubs/representative.png"
                    verify(exactly = 1) { fileStorageService.store(any(), "clubs") }
                }
            }
        }
    })
