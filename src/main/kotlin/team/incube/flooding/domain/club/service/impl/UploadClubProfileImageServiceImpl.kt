package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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

        val previousImageUrl = club.imageUrl
        val imageUrl = fileStorageService.store(image, "clubs")
        club.imageUrl = imageUrl
        registerFileCleanup(
            previousImageUrl = previousImageUrl,
            currentImageUrl = imageUrl,
        )

        return UploadClubProfileImageResponse(imageUrl)
    }

    private fun registerFileCleanup(
        previousImageUrl: String?,
        currentImageUrl: String,
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            previousImageUrl?.let { fileStorageService.delete(it) }
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    previousImageUrl?.let { fileStorageService.delete(it) }
                }

                override fun afterCompletion(status: Int) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        fileStorageService.delete(currentImageUrl)
                    }
                }
            },
        )
    }
}
