package team.incube.flooding.domain.club.presentation.data.response

import team.incube.flooding.domain.club.entity.ClubFormFieldType

data class GetClubFormResponse(
    val formId: Long,
    val title: String,
    val description: String?,
    val fields: List<GetClubFormFieldResponse>,
)

data class GetClubFormFieldResponse(
    val fieldId: Long,
    val label: String,
    val description: String?,
    val fieldType: ClubFormFieldType,
    val order: Int,
    val required: Boolean,
    val options: List<GetClubFormFieldOptionResponse>,
)

data class GetClubFormFieldOptionResponse(
    val optionId: Long,
    val label: String,
    val value: String,
)
