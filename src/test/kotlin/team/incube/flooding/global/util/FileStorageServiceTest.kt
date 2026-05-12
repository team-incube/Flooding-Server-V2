package team.incube.flooding.global.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.global.config.FileStorageConstants.IMAGE_URL_PREFIX
import team.incube.flooding.global.config.FileStorageProperties
import team.themoment.sdk.exception.ExpectedException
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files

class FileStorageServiceTest :
    BehaviorSpec({
        lateinit var uploadDir: java.nio.file.Path
        lateinit var service: FileStorageService

        beforeEach {
            uploadDir = Files.createTempDirectory("flooding-upload-test")
            service =
                FileStorageService(
                    FileStorageProperties(
                        uploadDir = uploadDir.toString(),
                        baseUrl = "https://dev-api.example.com",
                    ),
                )
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
                    val file = MockMultipartFile("image", "profile.png", "text/plain", pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("MIME 타입이 없는 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", null, pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("정상 이미지 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("이미지를 저장하고 접근 경로를 반환한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val imageUrl = service.store(file, "clubs")

                    imageUrl shouldMatch Regex("^https://dev-api\\.example\\.com/images/clubs/[0-9a-f-]+\\.png$")
                    val savedPath = URI.create(imageUrl).path.removePrefix("$IMAGE_URL_PREFIX/")
                    Files.exists(uploadDir.resolve(savedPath)).shouldBeTrue()
                }
            }
        }

        given("한 번만 열 수 있는 이미지 스트림이 주어졌을 때") {
            `when`("저장하면") {
                then("같은 스트림으로 검증과 저장을 수행한다") {
                    val file = SingleUseMultipartFile()

                    val imageUrl = service.store(file, "clubs")

                    val savedPath = URI.create(imageUrl).path.removePrefix("$IMAGE_URL_PREFIX/")
                    Files.readAllBytes(uploadDir.resolve(savedPath)) shouldBe pngBytes()
                }
            }
        }

        given("이미지 확장자와 MIME 타입이지만 파일 시그니처가 다를 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("상위 디렉터리로 벗어나는 subDir이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "../outside") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        given("파일 복사 중 예외가 발생했을 때") {
            `when`("저장하면") {
                then("부분 기록 파일을 정리하고 INTERNAL_SERVER_ERROR 예외가 발생한다") {
                    val file = FailingMultipartFile()

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                    Files.list(uploadDir.resolve("clubs")).use { files ->
                        files.count() shouldBe 0
                    }
                }
            }
        }

        given("저장된 이미지 URL이 주어졌을 때") {
            `when`("삭제하면") {
                then("파일이 제거된다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())
                    val imageUrl = service.store(file, "clubs")
                    val savedPath = URI.create(imageUrl).path.removePrefix("$IMAGE_URL_PREFIX/")

                    service.delete(imageUrl)

                    Files.notExists(uploadDir.resolve(savedPath)).shouldBeTrue()
                }
            }
        }
    })

private fun pngBytes(): ByteArray =
    byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
    )

private class SingleUseMultipartFile : MultipartFile {
    private var opened = false

    override fun getName(): String = "image"

    override fun getOriginalFilename(): String = "profile.png"

    override fun getContentType(): String = "image/png"

    override fun isEmpty(): Boolean = false

    override fun getSize(): Long = pngBytes().size.toLong()

    override fun getBytes(): ByteArray = pngBytes()

    override fun getInputStream(): InputStream {
        if (opened) {
            throw IOException("stream already consumed")
        }
        opened = true
        return pngBytes().inputStream()
    }

    override fun transferTo(dest: java.io.File) = Unit
}

private class FailingMultipartFile : MultipartFile {
    override fun getName(): String = "image"

    override fun getOriginalFilename(): String = "profile.png"

    override fun getContentType(): String = "image/png"

    override fun isEmpty(): Boolean = false

    override fun getSize(): Long = 1

    override fun getBytes(): ByteArray = pngBytes()

    override fun getInputStream(): InputStream =
        object : InputStream() {
            private val bytes = pngBytes()
            private var index = 0

            override fun read(): Int {
                if (index < bytes.size) {
                    return bytes[index++].toInt() and 0xFF
                }

                throw IOException("copy failed")
            }
        }

    override fun transferTo(dest: java.io.File) = Unit
}
