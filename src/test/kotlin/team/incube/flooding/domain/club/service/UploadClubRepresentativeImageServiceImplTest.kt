package team.incube.flooding.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.domain.club.service.impl.UploadClubRepresentativeImageServiceImpl
import team.incube.flooding.global.config.FileStorageConstants.CLUB_IMAGE_SUB_DIR
import team.incube.flooding.global.util.FileStorageService
import team.themoment.sdk.exception.ExpectedException

class UploadClubRepresentativeImageServiceImplTest :
    BehaviorSpec({
        val fileStorageService = mockk<FileStorageService>()
        val service = UploadClubRepresentativeImageServiceImpl(fileStorageService)

        beforeEach { clearAllMocks() }

        fun image() = MockMultipartFile("image", "representative.png", "image/png", byteArrayOf(1, 2, 3))

        given("동아리 대표 이미지 업로드 요청이 들어오면") {
            `when`("이미지를 저장한다") {
                then("저장된 이미지 URL을 반환한다") {
                    every { fileStorageService.store(any(), CLUB_IMAGE_SUB_DIR) } returns
                        "https://dev-api.example.com/images/clubs/representative.png"

                    val response = service.execute(image())

                    response.imageUrl shouldBe "https://dev-api.example.com/images/clubs/representative.png"
                    verify(exactly = 1) { fileStorageService.store(any(), CLUB_IMAGE_SUB_DIR) }
                }
            }

            `when`("파일 저장 서비스에서 예외가 발생하면") {
                then("예외를 그대로 전파한다") {
                    every { fileStorageService.store(any(), CLUB_IMAGE_SUB_DIR) } throws
                        ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)

                    val exception = shouldThrow<ExpectedException> { service.execute(image()) }

                    exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }
            }
        }
    })
