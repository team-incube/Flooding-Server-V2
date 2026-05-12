package team.incube.flooding.domain.club.service

import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.club.presentation.data.response.UploadClubRepresentativeImageResponse

interface UploadClubRepresentativeImageService {
    fun execute(image: MultipartFile): UploadClubRepresentativeImageResponse
}
