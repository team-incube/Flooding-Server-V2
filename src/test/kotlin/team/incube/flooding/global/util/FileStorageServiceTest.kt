package team.incube.flooding.global.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.incube.flooding.global.config.FileStorageProperties
import team.themoment.sdk.exception.ExpectedException
import java.nio.file.Files

class FileStorageServiceTest :
    BehaviorSpec({
        lateinit var uploadDir: java.nio.file.Path
        lateinit var service: FileStorageService

        beforeEach {
            uploadDir = Files.createTempDirectory("flooding-upload-test")
            service = FileStorageService(FileStorageProperties(uploadDir = uploadDir.toString()))
        }

        afterEach {
            uploadDir.toFile().deleteRecursively()
        }

        given("빈 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("지원하지 않는 확장자의 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.gif", "image/gif", byteArrayOf(1))

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("지원하지 않는 MIME 타입의 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "text/plain", byteArrayOf(1))

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("정상 이미지 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("이미지를 저장하고 접근 경로를 반환한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

                    val imageUrl = service.store(file, "clubs")

                    imageUrl shouldMatch Regex("^/images/clubs/[0-9a-f-]+\\.png$")
                    Files.exists(uploadDir.resolve(imageUrl.removePrefix("/images/"))).shouldBeTrue()
                }
            }
        }
    })
