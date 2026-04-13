package team.incube.flooding.domain.club.presentation.data.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import team.incube.flooding.domain.club.entity.ClubFormFieldType

data class CreateClubFormRequest(
    @field:NotBlank
    val title: String,
    val description: String?,
    @field:NotEmpty
    @field:Valid
    val fields: List<CreateClubFormFieldRequest>,
)

data class CreateClubFormFieldRequest(
    @field:NotBlank
    val label: String,
    val description: String?,
    val fieldType: ClubFormFieldType,
    val order: Int,
    val required: Boolean,
    val options: List<CreateClubFormFieldOptionRequest>?,
)

data class CreateClubFormFieldOptionRequest(
    @field:NotBlank
    val label: String,
    @field:NotBlank
    val value: String,
)
