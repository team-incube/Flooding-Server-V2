package team.incube.flooding.domain.club.presentation.data.request

import jakarta.validation.constraints.NotBlank

data class PutClubRequest(
    @field:NotBlank
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val maxMember: Int?,
)
