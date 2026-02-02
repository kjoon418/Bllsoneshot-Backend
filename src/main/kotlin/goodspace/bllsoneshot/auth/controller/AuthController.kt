package goodspace.bllsoneshot.auth.controller

import goodspace.bllsoneshot.auth.dto.LoginRequest
import goodspace.bllsoneshot.auth.dto.LoginResponse
import goodspace.bllsoneshot.auth.service.AuthService
import goodspace.bllsoneshot.global.cookie.RefreshTokenCookieProvider
import goodspace.bllsoneshot.global.cookie.setCookie
import goodspace.bllsoneshot.global.security.TokenProvider
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val tokenProvider: TokenProvider,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider
) {

    @PostMapping("/login")
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
                accessToken = result.accessToken
            )
        )
    }

    companion object {
        const val SET_COOKIE = "Set-Cookie"
    }
}
