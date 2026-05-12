package team.incube.flooding.domain.club.service.impl

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.club.presentation.data.response.UploadClubRepresentativeImageResponse
import team.incube.flooding.domain.club.service.UploadClubRepresentativeImageService
import team.incube.flooding.global.config.FileStorageConstants.CLUB_IMAGE_SUB_DIR
import team.incube.flooding.global.util.FileStorageService

@Service
class UploadClubRepresentativeImageServiceImpl(
    private val fileStorageService: FileStorageService,
) : UploadClubRepresentativeImageService {
    override fun execute(image: MultipartFile): UploadClubRepresentativeImageResponse {
        val imageUrl = fileStorageService.store(image, CLUB_IMAGE_SUB_DIR)
        return UploadClubRepresentativeImageResponse(imageUrl)
    }
}
