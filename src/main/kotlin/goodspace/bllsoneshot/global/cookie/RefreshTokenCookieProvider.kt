package goodspace.bllsoneshot.global.cookie

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class RefreshTokenCookieProvider(
    @Value("\${app.auth.refresh-token-cookie.name}")
    private val name: String,
    @Value("\${app.auth.refresh-token-cookie.path}")
    private val path: String,
    @Value("\${app.auth.refresh-token-cookie.secure}")
    private val secure: Boolean,
    @Value("\${app.auth.refresh-token-cookie.same-site}")
    private val sameSite: String
) {

    fun create(refreshToken: String, maxAgeSeconds: Long): ResponseCookie {
        return ResponseCookie.from(name, refreshToken)
            .httpOnly(true)
            .secure(secure)
            .path(path)
            .sameSite(sameSite)
            .maxAge(maxAgeSeconds)
            .build()
    }
}
