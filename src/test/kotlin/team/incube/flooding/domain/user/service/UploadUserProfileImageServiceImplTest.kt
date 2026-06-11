package team.incube.flooding.domain.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.impl.UploadUserProfileImageServiceImpl
import team.incube.flooding.global.config.FileStorageConstants.USER_PROFILE_IMAGE_SUB_DIR
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.incube.flooding.global.util.FileStorageService
import team.themoment.sdk.exception.ExpectedException

class UploadUserProfileImageServiceImplTest :
    BehaviorSpec({
        val fileStorageService = mockk<FileStorageService>()
        val currentUserProvider = mockk<CurrentUserProvider>()
        val userRepository = mockk<UserRepository>()
        val service = UploadUserProfileImageServiceImpl(fileStorageService, currentUserProvider, userRepository)

        beforeEach { clearAllMocks() }

        fun image() = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

        fun user(profileImageUrl: String? = null) =
            UserJpaEntity(
                id = 1L,
                name = "홍길동",
                sex = Sex.MAN,
                email = "test@test.com",
                studentNumber = 10101,
                role = Role.GENERAL_STUDENT,
                dormitoryRoom = 101,
                profileImageUrl = profileImageUrl,
            )

        given("유저 프로필 이미지 업로드 요청이 들어오면") {
            `when`("기존 프로필 이미지가 없고 정상 이미지를 전달하면") {
                then("저장된 이미지 URL을 반환하고 엔티티를 저장한다") {
                    val user = user()
                    every { currentUserProvider.getCurrentUser() } returns user
                    justRun { fileStorageService.delete(null) }
                    every { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) } returns
                        "https://image-dev.flooding.kr/users/profiles/profile.png"
                    every { userRepository.save(any()) } returns user

                    val response = service.execute(image())

                    response.profileImageUrl shouldBe "https://image-dev.flooding.kr/users/profiles/profile.png"
                    verify(exactly = 1) { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) }
                    verify(exactly = 1) { userRepository.save(any()) }
                }
            }

            `when`("기존 프로필 이미지가 있고 새 이미지를 전달하면") {
                then("기존 이미지를 S3에서 삭제하고 새 URL을 저장한다") {
                    val oldUrl = "https://image-dev.flooding.kr/users/profiles/old.png"
                    val user = user(profileImageUrl = oldUrl)
                    every { currentUserProvider.getCurrentUser() } returns user
                    justRun { fileStorageService.delete(oldUrl) }
                    every { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) } returns
                        "https://image-dev.flooding.kr/users/profiles/new.png"
                    every { userRepository.save(any()) } returns user

                    val response = service.execute(image())

                    response.profileImageUrl shouldBe "https://image-dev.flooding.kr/users/profiles/new.png"
                    verify(exactly = 1) { fileStorageService.delete(oldUrl) }
                    verify(exactly = 1) { userRepository.save(any()) }
                }
            }

            `when`("파일 저장에 실패하면") {
                then("예외를 그대로 전파한다") {
                    val user = user()
                    every { currentUserProvider.getCurrentUser() } returns user
                    justRun { fileStorageService.delete(null) }
                    every { fileStorageService.store(any(), USER_PROFILE_IMAGE_SUB_DIR) } throws
                        ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)

                    val exception = shouldThrow<ExpectedException> { service.execute(image()) }

                    exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }
            }
        }
    })
