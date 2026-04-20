package team.incube.flooding.domain.auth.dto.response

data class ReissueTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
