package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.incube.flooding.domain.club.presentation.data.response.GetClubFormFieldOptionResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubFormFieldResponse
import team.incube.flooding.domain.club.presentation.data.response.GetClubFormResponse
import team.incube.flooding.domain.club.repository.ClubFormFieldOptionRepository
import team.incube.flooding.domain.club.repository.ClubFormFieldRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.service.GetClubFormService
import team.themoment.sdk.exception.ExpectedException

@Service
class GetClubFormServiceImpl(
    private val clubFormRepository: ClubFormRepository,
    private val clubFormFieldRepository: ClubFormFieldRepository,
    private val clubFormFieldOptionRepository: ClubFormFieldOptionRepository,
) : GetClubFormService {
    override fun execute(clubId: Long): GetClubFormResponse {
        val form =
            clubFormRepository.findByClubIdAndIsActiveTrue(clubId)
                ?: throw ExpectedException("활성화된 신청 폼이 없습니다.", HttpStatus.NOT_FOUND)

        val fields = clubFormFieldRepository.findAllByFormIdOrderByFieldOrder(form.id)
        val optionsByFieldId =
            clubFormFieldOptionRepository
                .findAllByFieldIdInOrderByOptionOrder(fields.map { it.id })
                .groupBy { it.field.id }

        return GetClubFormResponse(
            formId = form.id,
            title = form.title,
            description = form.description,
            fields =
                fields.map { field ->
                    GetClubFormFieldResponse(
                        fieldId = field.id,
                        label = field.label,
                        description = field.description,
                        fieldType = field.fieldType,
                        order = field.fieldOrder,
                        required = field.isRequired,
                        options =
                            optionsByFieldId[field.id]?.map { option ->
                                GetClubFormFieldOptionResponse(
                                    optionId = option.id,
                                    label = option.label,
                                    value = option.value,
                                )
                            } ?: emptyList(),
                    )
                },
        )
    }
}
