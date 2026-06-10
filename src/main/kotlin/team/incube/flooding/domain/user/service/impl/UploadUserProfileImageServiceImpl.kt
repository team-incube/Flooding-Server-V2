package team.incube.flooding.domain.user.service.impl

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.user.presentation.data.response.UploadUserProfileImageResponse
import team.incube.flooding.domain.user.service.UploadUserProfileImageService
import team.incube.flooding.global.config.FileStorageConstants.USER_PROFILE_IMAGE_SUB_DIR
import team.incube.flooding.global.util.FileStorageService

@Service
class UploadUserProfileImageServiceImpl(
    private val fileStorageService: FileStorageService,
) : UploadUserProfileImageService {
    override fun execute(image: MultipartFile): UploadUserProfileImageResponse {
        val profileImageUrl = fileStorageService.store(image, USER_PROFILE_IMAGE_SUB_DIR)
        return UploadUserProfileImageResponse(profileImageUrl)
    }
}
