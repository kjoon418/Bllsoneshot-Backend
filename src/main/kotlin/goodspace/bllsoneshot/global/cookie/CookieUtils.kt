package goodspace.bllsoneshot.global.cookie

import goodspace.bllsoneshot.auth.controller.AuthController.Companion.SET_COOKIE
import jakarta.servlet.http.HttpServletResponse

fun HttpServletResponse.setCookie(value: String) {
    addHeader(SET_COOKIE, value)
}
