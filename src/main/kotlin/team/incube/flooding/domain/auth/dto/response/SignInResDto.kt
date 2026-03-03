package team.incube.flooding.domain.auth.dto.response

data class SignInResDto(
    val accessToken: String,
    val refreshToken: String
)
