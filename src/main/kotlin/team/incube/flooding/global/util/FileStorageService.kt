package team.incube.flooding.global.util

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.global.config.FileStorageConstants.IMAGE_URL_PREFIX
import team.incube.flooding.global.config.FileStorageProperties
import team.themoment.sdk.exception.ExpectedException
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService(
    private val fileStorageProperties: FileStorageProperties,
) {
    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")
    private val allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp")

    fun store(
        file: MultipartFile,
        subDir: String,
    ): String {
        validate(file)

        val extension = getExtension(file)
        val fileName = "${UUID.randomUUID()}.$extension"
        val targetDir = Paths.get(fileStorageProperties.uploadDir, subDir).toAbsolutePath().normalize()
        val targetPath = targetDir.resolve(fileName).normalize()

        if (!targetPath.startsWith(targetDir)) {
            throw ExpectedException("파일 저장 경로가 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        try {
            Files.createDirectories(targetDir)
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (exception: IOException) {
            runCatching {
                Files.deleteIfExists(targetPath)
            }
            throw ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return "${fileStorageProperties.baseUrl.trimEnd('/')}$IMAGE_URL_PREFIX/$subDir/$fileName"
    }

    fun delete(imageUrl: String?) {
        val relativePath = getStoredRelativePath(imageUrl) ?: return
        val uploadRoot = Paths.get(fileStorageProperties.uploadDir).toAbsolutePath().normalize()
        val targetPath = uploadRoot.resolve(relativePath).normalize()

        if (!targetPath.startsWith(uploadRoot)) return

        runCatching {
            Files.deleteIfExists(targetPath)
        }
    }

    private fun validate(file: MultipartFile) {
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

    private fun getStoredRelativePath(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        val path =
            runCatching { URI.create(imageUrl).path }
                .getOrDefault(imageUrl)

        if (!path.startsWith("$IMAGE_URL_PREFIX/")) return null

        return path.removePrefix("$IMAGE_URL_PREFIX/")
    }
}
