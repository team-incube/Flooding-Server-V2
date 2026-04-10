package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubFormFieldJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormFieldOptionJpaEntity
import team.incube.flooding.domain.club.entity.ClubFormJpaEntity
import team.incube.flooding.domain.club.presentation.data.request.CreateClubFormRequest
import team.incube.flooding.domain.club.presentation.data.response.CreateClubFormResponse
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.CreateClubFormService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateClubFormServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubFormRepository: ClubFormRepository,
    private val clubFormFieldRepository: ClubFormFieldRepository,
    private val clubFormFieldOptionRepository: ClubFormFieldOptionRepository,
) : CreateClubFormService {
    @Transactional
    override fun execute(
        clubId: Long,
        request: CreateClubFormRequest,
    ): CreateClubFormResponse {
        val club =
            clubRepository.findById(clubId).orElseThrow {
                ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        clubFormRepository.findByClubIdAndIsActiveTrue(clubId)?.let {
            it.isActive = false
        }

        val form =
            clubFormRepository.save(
                ClubFormJpaEntity(
                    club = club,
                    title = request.title,
                    description = request.description,
                ),
            )

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
            savedFields.zip(request.fields).flatMapIndexed { _, (savedField, fieldRequest) ->
                fieldRequest.options?.mapIndexed { index, optionRequest ->
                    ClubFormFieldOptionJpaEntity(
                        field = savedField,
                        label = optionRequest.label,
                        value = optionRequest.value,
                        optionOrder = index,
                    )
                } ?: emptyList()
            },
        )

        return CreateClubFormResponse(formId = form.id)
    }
}
