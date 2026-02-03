package goodspace.bllsoneshot.global.cookie

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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

    fun addToCookie(
        refreshToken: String,
        maxAgeSeconds: Long,
        response: HttpServletResponse
    ) {
        val cookie = ResponseCookie.from(name, refreshToken)
            .httpOnly(true)
            .secure(secure)
            .path(path)
            .sameSite(sameSite)
            .maxAge(maxAgeSeconds)
            .build()

        response.addHeader(SET_COOKIE, cookie.toString())
    }

    fun extract(request: HttpServletRequest): String? {
        val cookies = request.cookies ?: return null

        return cookies
            .firstOrNull { it.name == name }
            ?.takeIf { it.value.isNotBlank() }
            ?.value
    }

    companion object {
        const val SET_COOKIE = "Set-Cookie"
    }
}
