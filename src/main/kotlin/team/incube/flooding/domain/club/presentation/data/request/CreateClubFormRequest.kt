package team.incube.flooding.domain.club.presentation.data.request

import team.incube.flooding.domain.club.entity.ClubFormFieldType

data class CreateClubFormRequest(
    val clubId: Long,
    val title: String,
    val description: String?,
    val fields: List<CreateClubFormFieldRequest>,
)

data class CreateClubFormFieldRequest(
    val label: String,
    val description: String?,
    val fieldType: ClubFormFieldType,
    val order: Int,
    val required: Boolean,
    val options: List<CreateClubFormFieldOptionRequest>?,
)

data class CreateClubFormFieldOptionRequest(
    val label: String,
    val value: String,
)
