package team.incube.flooding.domain.club.presentation.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PutClubRequest(
    @field:NotBlank
    val name: String,
    val description: String?,
    val imageUrl: String?,
    @field:Positive
    val maxMember: Int?,
)
