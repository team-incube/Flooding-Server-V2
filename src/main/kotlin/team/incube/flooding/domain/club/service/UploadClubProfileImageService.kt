package team.incube.flooding.domain.club.service

import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.club.presentation.data.response.UploadClubProfileImageResponse

interface UploadClubProfileImageService {
    fun execute(
        clubId: Long,
        image: MultipartFile,
    ): UploadClubProfileImageResponse
}
