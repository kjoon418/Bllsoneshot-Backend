package goodspace.bllsoneshot.auth.dto

data class LoginResult(
    val accessToken: String,
    val refreshToken: String
)
