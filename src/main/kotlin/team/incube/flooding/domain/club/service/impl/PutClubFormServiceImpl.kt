package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity
import team.incube.flooding.domain.club.presentation.data.request.PutClubFormRequest
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.PutClubFormService
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class PutClubFormServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubFormRepository: ClubFormRepository,
    private val clubFormFieldRepository: ClubFormFieldRepository,
    private val clubFormFieldOptionRepository: ClubFormFieldOptionRepository,
    private val submissionRepository: ClubFormSubmissionRepository,
    private val currentUserProvider: CurrentUserProvider,
) : PutClubFormService {
    @Transactional
    override fun execute(
        clubId: Long,
        request: PutClubFormRequest,
    ) {
        val currentUser = currentUserProvider.getCurrentUser()
        val club =
            clubRepository.findByIdWithLeader(clubId)
                ?: throw ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (club.leader?.id != currentUser.id) {
            throw ExpectedException("해당 동아리의 관리자만 폼을 수정할 수 있습니다.", HttpStatus.FORBIDDEN)
        }
        val form =
            clubFormRepository.findByClubIdAndIsActiveTrue(clubId)
                ?: throw ExpectedException("생성된 폼이 없습니다.", HttpStatus.NOT_FOUND)
        if (submissionRepository.existsByFormId(form.id)) {
            throw ExpectedException("신청자가 있는 폼은 수정할 수 없습니다.", HttpStatus.CONFLICT)
        }

        form.title = request.title
        form.description = request.description

        clubFormFieldOptionRepository.deleteAllByFormId(form.id)
        clubFormFieldRepository.deleteAllByFormId(form.id)

        val savedFields =
            clubFormFieldRepository.saveAll(
                request.fields.map { fieldRequest ->
                    ClubFormFieldJpaEntity(
                        form = form,
                        label = fieldRequest.label,
                        description = fieldRequest.description,
                        fieldType = fieldRequest.fieldType,
                        fieldOrder = fieldRequest.order,
                        isRequired = fieldRequest.required,
                    )
                },
            )

        clubFormFieldOptionRepository.saveAll(
            savedFields.zip(request.fields).flatMap { (savedField, fieldRequest) ->
                fieldRequest.options?.mapIndexed { index, opt ->
                    ClubFormFieldOptionJpaEntity(
                        field = savedField,
                        label = opt.label,
                        value = opt.value,
                        optionOrder = index,
                    )
                } ?: emptyList()
            },
        )
    }
}
