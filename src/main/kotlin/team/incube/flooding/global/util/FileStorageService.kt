package team.incube.flooding.global.util

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import team.incube.flooding.global.config.FileStorageProperties
import team.themoment.sdk.exception.ExpectedException
import java.io.IOException
import java.io.PushbackInputStream
import java.util.UUID

@Component
class FileStorageService(
    private val fileStorageProperties: FileStorageProperties,
    private val s3Client: S3Client,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun store(
        file: MultipartFile,
        subDir: String,
    ): String {
        val extension = validateAndGetExtension(file)
        val fileName = "${UUID.randomUUID()}.$extension"
        val objectKey = getObjectKey(subDir, fileName)

        try {
            file.inputStream.use { rawInputStream ->
                val inputStream = PushbackInputStream(rawInputStream, IMAGE_SIGNATURE_READ_SIZE)
                validateImageSignature(inputStream)
                s3Client.putObject(
                    PutObjectRequest
                        .builder()
                        .bucket(fileStorageProperties.bucket)
                        .key(objectKey)
                        .contentType(file.contentType)
                        .cacheControl("public, max-age=31536000, immutable")
                        .build(),
                    RequestBody.fromInputStream(inputStream, file.size),
                )
            }
        } catch (exception: ExpectedException) {
            throw exception
        } catch (exception: IOException) {
            throw ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (exception: SdkException) {
            throw ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return "${fileStorageProperties.publicBaseUrl.trimEnd('/')}/$objectKey"
    }

    fun delete(imageUrl: String?) {
        val objectKey = getStoredObjectKey(imageUrl) ?: return

        runCatching {
            s3Client.deleteObject(
                DeleteObjectRequest
                    .builder()
                    .bucket(fileStorageProperties.bucket)
                    .key(objectKey)
                    .build(),
            )
        }.onFailure { exception ->
            log.warn("R2 이미지 삭제에 실패했습니다. key={}", objectKey, exception)
        }
    }

    private fun validateAndGetExtension(file: MultipartFile): String {
        if (file.isEmpty) {
            throw ExpectedException("이미지 파일이 비어 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val extension = getExtension(file)
        if (extension !in allowedExtensions) {
            throw ExpectedException("지원하지 않는 이미지 확장자입니다.", HttpStatus.BAD_REQUEST)
        }

        if (file.contentType !in allowedContentTypes) {
            throw ExpectedException("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST)
        }

        return extension
    }

    private fun getObjectKey(
        subDir: String,
        fileName: String,
    ): String {
        val segments = subDir.trim('/').split("/")

        if (segments.isEmpty() || segments.any { it.isBlank() || it == "." || it == ".." || it.contains("\\") }) {
            throw ExpectedException("파일 저장 경로가 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        return "${segments.joinToString("/")}/$fileName"
    }

    private fun getExtension(file: MultipartFile): String {
        val originalFilename =
            file.originalFilename
                ?: throw ExpectedException("파일명이 존재하지 않습니다.", HttpStatus.BAD_REQUEST)
        val extension = originalFilename.substringAfterLast('.', "").lowercase()

        if (extension.isBlank()) {
            throw ExpectedException("파일 확장자가 존재하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        return extension
    }

    private fun validateImageSignature(inputStream: PushbackInputStream) {
        val header =
            runCatching {
                val bytes = ByteArray(IMAGE_SIGNATURE_READ_SIZE)
                val bytesRead = inputStream.readNBytes(bytes, 0, IMAGE_SIGNATURE_READ_SIZE)

                if (bytesRead > 0) {
                    inputStream.unread(bytes, 0, bytesRead)
                }

                bytes.copyOf(bytesRead)
            }.getOrElse { throw ExpectedException("이미지 파일을 읽을 수 없습니다.", HttpStatus.BAD_REQUEST) }

        if (!hasValidImageSignature(header)) {
            throw ExpectedException("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST)
        }
    }

    private fun hasValidImageSignature(bytes: ByteArray): Boolean = isJpeg(bytes) || isPng(bytes) || isWebp(bytes)

    private fun isJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

    private fun isPng(bytes: ByteArray): Boolean =
        bytes.size >= PNG_SIGNATURE.size &&
            PNG_SIGNATURE.indices.all { bytes[it] == PNG_SIGNATURE[it] }

    private fun isWebp(bytes: ByteArray): Boolean =
        bytes.size >= 12 &&
            bytes.copyOfRange(0, 4).contentEquals("RIFF".toByteArray()) &&
            bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray())

    private fun getStoredObjectKey(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        val publicBaseUrl = fileStorageProperties.publicBaseUrl.trimEnd('/')

        if (!imageUrl.startsWith("$publicBaseUrl/")) return null

        val objectKey =
            imageUrl
                .removePrefix("$publicBaseUrl/")
                .substringBefore("?")
                .substringBefore("#")

        if (objectKey.isBlank()) return null
        if (objectKey.split("/").any { it.isBlank() || it == "." || it == ".." || it.contains("\\") }) return null

        return objectKey
    }

    companion object {
        private const val IMAGE_SIGNATURE_READ_SIZE = 12
        private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")
        private val allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp")

        private val PNG_SIGNATURE =
            byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
            )
    }
}
