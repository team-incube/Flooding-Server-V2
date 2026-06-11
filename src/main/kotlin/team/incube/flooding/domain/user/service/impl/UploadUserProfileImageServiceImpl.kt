package team.incube.flooding.domain.user.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.user.presentation.data.response.UploadUserProfileImageResponse
import team.incube.flooding.domain.user.repository.UserRepository
import team.incube.flooding.domain.user.service.UploadUserProfileImageService
import team.incube.flooding.global.config.FileStorageConstants.USER_PROFILE_IMAGE_SUB_DIR
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.incube.flooding.global.util.FileStorageService

@Service
class UploadUserProfileImageServiceImpl(
    private val fileStorageService: FileStorageService,
    private val currentUserProvider: CurrentUserProvider,
    private val userRepository: UserRepository,
) : UploadUserProfileImageService {
    @Transactional
    override fun execute(image: MultipartFile): UploadUserProfileImageResponse {
        val user = currentUserProvider.getCurrentUser()
        fileStorageService.delete(user.profileImageUrl)
        val profileImageUrl = fileStorageService.store(image, USER_PROFILE_IMAGE_SUB_DIR)
        user.profileImageUrl = profileImageUrl
        userRepository.save(user)
        return UploadUserProfileImageResponse(profileImageUrl)
    }
}
