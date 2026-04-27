package team.incube.flooding.domain.club.presentation.data.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class PutClubFormRequest(
    @field:NotBlank
    val title: String,
    val description: String?,
    @field:NotEmpty
    @field:Valid
    val fields: List<CreateClubFormFieldRequest>,
)
