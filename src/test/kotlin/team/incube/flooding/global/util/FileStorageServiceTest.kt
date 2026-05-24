package team.incube.flooding.global.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import team.incube.flooding.global.config.FileStorageProperties
import team.themoment.sdk.exception.ExpectedException

class FileStorageServiceTest :
    BehaviorSpec({
        lateinit var s3Client: S3Client
        lateinit var service: FileStorageService

        beforeEach {
            s3Client = mockk()
            service =
                FileStorageService(
                    FileStorageProperties(
                        bucket = "flooding-dev-images",
                        endpoint = "https://account-id.r2.cloudflarestorage.com",
                        accessKey = "access-key",
                        secretKey = "secret-key",
                        publicBaseUrl = "https://image-dev.flooding.kr",
                    ),
                    s3Client,
                )
        }

        afterEach { clearAllMocks() }

        given("빈 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("지원하지 않는 확장자의 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.gif", "image/gif", byteArrayOf(1))

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("지원하지 않는 MIME 타입의 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "text/plain", pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("MIME 타입이 없는 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", null, pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("정상 이미지 파일이 주어졌을 때") {
            `when`("저장하면") {
                then("R2에 이미지를 저장하고 공개 URL을 반환한다") {
                    val requestSlot = slot<PutObjectRequest>()
                    every { s3Client.putObject(capture(requestSlot), any<RequestBody>()) } returns
                        PutObjectResponse.builder().eTag("etag").build()
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val imageUrl = service.store(file, "clubs")

                    imageUrl shouldMatch Regex("^https://image-dev\\.flooding\\.kr/clubs/[0-9a-f-]+\\.png$")
                    requestSlot.captured.bucket() shouldBe "flooding-dev-images"
                    requestSlot.captured.key() shouldMatch Regex("^clubs/[0-9a-f-]+\\.png$")
                    requestSlot.captured.contentType() shouldBe "image/png"
                }
            }
        }

        given("정상 이미지 bytes가 주어졌을 때") {
            `when`("저장하면") {
                then("검증한 bytes를 R2 업로드 본문으로 사용한다") {
                    every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } answers {
                        secondArg<RequestBody>().contentStreamProvider().newStream().use { inputStream ->
                            inputStream.readAllBytes() shouldBe pngBytes()
                        }
                        PutObjectResponse.builder().eTag("etag").build()
                    }
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val imageUrl = service.store(file, "clubs")

                    imageUrl shouldMatch Regex("^https://image-dev\\.flooding\\.kr/clubs/[0-9a-f-]+\\.png$")
                }
            }
        }

        given("이미지 확장자와 MIME 타입이지만 파일 시그니처가 다를 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", byteArrayOf(1, 2, 3))

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("상위 디렉터리로 벗어나는 subDir이 주어졌을 때") {
            `when`("저장하면") {
                then("BAD_REQUEST 예외가 발생한다") {
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "../outside") }

                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
                }
            }
        }

        given("R2 저장 중 예외가 발생했을 때") {
            `when`("저장하면") {
                then("INTERNAL_SERVER_ERROR 예외가 발생한다") {
                    every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } throws
                        SdkClientException.create("upload failed")
                    val file = MockMultipartFile("image", "profile.png", "image/png", pngBytes())

                    val exception = shouldThrow<ExpectedException> { service.store(file, "clubs") }

                    exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }
            }
        }

        given("저장된 이미지 URL이 주어졌을 때") {
            `when`("삭제하면") {
                then("R2 object가 제거된다") {
                    val requestSlot = slot<DeleteObjectRequest>()
                    every { s3Client.deleteObject(capture(requestSlot)) } returns DeleteObjectResponse.builder().build()

                    service.delete("https://image-dev.flooding.kr/clubs/test.png")

                    requestSlot.captured.bucket() shouldBe "flooding-dev-images"
                    requestSlot.captured.key() shouldBe "clubs/test.png"
                }
            }
        }

        given("공개 URL 기준을 벗어나는 이미지 URL이 주어졌을 때") {
            `when`("삭제하면") {
                then("R2 삭제 요청을 보내지 않는다") {
                    service.delete("https://image-dev.flooding.kr/../outside.png")
                    service.delete("https://other.example.com/clubs/test.png")

                    verify(exactly = 0) { s3Client.deleteObject(any<DeleteObjectRequest>()) }
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
