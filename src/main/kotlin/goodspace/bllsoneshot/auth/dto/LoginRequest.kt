package goodspace.bllsoneshot.auth.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "ID가 비어 있습니다.")
    val loginId: String,

    @field:NotBlank(message = "비밀번호가 비어 있습니다.")
    val password: String
)
