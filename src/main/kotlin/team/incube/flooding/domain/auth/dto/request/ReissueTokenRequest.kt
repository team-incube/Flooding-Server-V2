package team.incube.flooding.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class ReissueTokenRequest(
    @field:NotBlank
    val refreshToken: String,
)
