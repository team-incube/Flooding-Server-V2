package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubFormAnswerJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormSubmissionJpaEntity
import team.incube.flooding.domain.club.presentation.data.request.CreateClubApplicationRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubApplicationResponse
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.service.CreateClubApplicationService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateClubApplicationServiceImpl(
    private val clubFormRepository: ClubFormRepository,
    private val clubFormFieldRepository: ClubFormFieldRepository,
    private val clubFormSubmissionRepository: ClubFormSubmissionRepository,
    private val clubFormAnswerRepository: ClubFormAnswerRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateClubApplicationService {
    @Transactional
    override fun execute(
        clubId: Long,
        request: CreateClubApplicationRequest,
    ): CreateClubApplicationResponse {
        val user = currentUserProvider.getCurrentUser()

        val form =
            clubFormRepository.findByClubIdAndIsActiveTrue(clubId)
                ?: throw ExpectedException("활성화된 신청 폼이 없습니다.", HttpStatus.NOT_FOUND)

        if (clubFormSubmissionRepository.existsByFormIdAndUserId(form.id, user.id)) {
            throw ExpectedException("이미 신청한 동아리입니다.", HttpStatus.CONFLICT)
        }

        val fields = clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(form.id)
        val answersByFieldId = request.answers.associateBy { it.fieldId }

        val missingLabels = fields
            .filter { it.isRequired }
            .filter { field -> answersByFieldId[field.id]?.value.isNullOrBlank() }
            .map { it.label }

        if (missingLabels.isNotEmpty()) {
            throw ExpectedException(
                "${missingLabels.joinToString(", ")} 항목은 필수입니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        val submission =
            clubFormSubmissionRepository.save(
                ClubFormSubmissionJpaEntity(
                    form = form,
                    user = user,
                ),
            )

        clubFormAnswerRepository.saveAll(
            fields.map { field ->
                ClubFormAnswerJpaEntity(
                    submission = submission,
                    field = field,
                    value = answersByFieldId[field.id]?.value,
                )
            },
        )

        return CreateClubApplicationResponse(applicationId = submission.id)
    }
}
