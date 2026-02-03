package goodspace.bllsoneshot.auth.controller

import goodspace.bllsoneshot.auth.dto.LoginRequest
import goodspace.bllsoneshot.auth.dto.LoginResponse
import goodspace.bllsoneshot.auth.service.AuthService
import goodspace.bllsoneshot.global.cookie.RefreshTokenCookieProvider
import goodspace.bllsoneshot.global.cookie.setCookie
import goodspace.bllsoneshot.global.security.TokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(
    name = "Auth API"
)
class AuthController(
    private val authService: AuthService,
    private val tokenProvider: TokenProvider,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider
) {

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = """
            아이디와 비밀번호를 기반으로 로그인합니다.
            멘토 회원인지 멘티 회원인지 정보를 같이 제공합니다.
            
            리프레쉬 토큰은 쿠키에 저장됩니다.
            
            role: ROLE_MENTOR, ROLE_MENTEE
        """
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val result = authService.login(request)

        val refreshTokenCookie = refreshTokenCookieProvider.create(
            refreshToken = result.refreshToken,
            maxAgeSeconds = tokenProvider.getRefreshTokenValiditySeconds()
        )
        response.setCookie(refreshTokenCookie.toString())

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = result.accessToken,
                role = result.role
            )
        )
    }

    companion object {
        const val SET_COOKIE = "Set-Cookie"
    }
}
