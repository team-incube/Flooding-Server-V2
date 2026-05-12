package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.incube.flooding.domain.club.presentation.data.response.UploadClubProfileImageResponse
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.UploadClubProfileImageService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.incube.flooding.global.util.FileStorageService
import team.themoment.sdk.exception.ExpectedException

@Service
class UploadClubProfileImageServiceImpl(
    private val clubRepository: ClubRepository,
    private val fileStorageService: FileStorageService,
    private val currentUserProvider: CurrentUserProvider,
) : UploadClubProfileImageService {
    @Transactional
    override fun execute(
        clubId: Long,
        image: MultipartFile,
    ): UploadClubProfileImageResponse {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)
            }
        val currentUser = currentUserProvider.getCurrentUser()

        if (!club.isModifiableBy(currentUser)) {
            throw ExpectedException("동아리를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        val imageUrl = fileStorageService.store(image, "clubs")
        club.imageUrl = imageUrl
        return UploadClubProfileImageResponse(imageUrl)
    }
}
