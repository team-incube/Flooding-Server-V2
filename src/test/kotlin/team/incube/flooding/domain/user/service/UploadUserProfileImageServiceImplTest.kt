package team.incube.flooding.domain.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.domain.user.service.impl.UploadUserProfileImageServiceImpl
import team.incube.flooding.global.config.FileStorageConstants.USER_PROFILE_IMAGE_SUB_DIR
import team.incube.flooding.global.util.FileStorageService
import team.themoment.sdk.exception.ExpectedException

class UploadUserProfileImageServiceImplTest :
    BehaviorSpec({
        val fileStorageService = mockk<FileStorageService>()
        val service = UploadUserProfileImageServiceImpl(fileStorageService)

        beforeEach { clearAllMocks() }

        fun image() = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

        given("유저 프로필 이미지 업로드 요청이 들어오면") {
            `when`("정상 이미지 파일을 전달하면") {
                then("저장된 이미지 URL을 반환한다") {
                    every { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) } returns
                        "https://image-dev.flooding.kr/users/profiles/profile.png"

                    val response = service.execute(image())

                    response.profileImageUrl shouldBe "https://image-dev.flooding.kr/users/profiles/profile.png"
                    verify(exactly = 1) { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) }
                }
            }

            `when`("파일 저장에 실패하면") {
                then("예외를 그대로 전파한다") {
                    every { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) } throws
                        ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)

                    val exception = shouldThrow<ExpectedException> { service.execute(image()) }

                    exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }
            }
        }
    })
