package team.incube.flooding.domain.auth.dto.response

data class SignInResponse(
    val accessToken: String,
    val refreshToken: String
)
