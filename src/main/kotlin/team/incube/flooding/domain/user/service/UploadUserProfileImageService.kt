package team.incube.flooding.domain.user.service

import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.user.presentation.data.response.UploadUserProfileImageResponse

interface UploadUserProfileImageService {
    fun execute(image: MultipartFile): UploadUserProfileImageResponse
}
