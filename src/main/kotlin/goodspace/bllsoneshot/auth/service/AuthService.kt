package goodspace.bllsoneshot.auth.service

import goodspace.bllsoneshot.auth.dto.LoginRequest
import goodspace.bllsoneshot.auth.dto.LoginResult
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.global.security.TokenProvider
import goodspace.bllsoneshot.global.security.TokenType
import goodspace.bllsoneshot.repository.user.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
) {

    @Transactional
    fun login(request: LoginRequest): LoginResult {
        val user = findUserByLoginId(request.loginId)
        validatePassword(request.password, user.password)

        val userId = user.id!!
        val accessToken = tokenProvider.createToken(userId, TokenType.ACCESS, user.role)
        val newRefreshToken = rotateRefreshToken(user)

        return LoginResult(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            role = user.role
        )
    }

    @Transactional
    fun reissueAccessToken(refreshToken: String?): LoginResult {
        validateToken(refreshToken)

        val user = findUserByToken(refreshToken!!)
        if (user.refreshToken != refreshToken) {
            throw BadCredentialsException(ExceptionMessage.INVALID_REFRESH_TOKEN.message)
        }

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.role)
        val newRefreshToken = rotateRefreshToken(user)

        return LoginResult(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            role = user.role
        )
    }

    private fun findUserByLoginId(loginId: String): User {
        return userRepository.findByLoginId(loginId)
            ?: throw IllegalArgumentException(ExceptionMessage.LOGIN_FAILED.message)
    }

    private fun validatePassword(
        requestPassword: String,
        actualPassword: String
    ) {
        if (!passwordEncoder.matches(requestPassword, actualPassword)) {
            throw IllegalArgumentException(ExceptionMessage.LOGIN_FAILED.message)
        }
    }

    private fun validateToken(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) {
            throw BadCredentialsException(ExceptionMessage.INVALID_REFRESH_TOKEN.message)
        }
        if (!tokenProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            throw BadCredentialsException(ExceptionMessage.INVALID_REFRESH_TOKEN.message)
        }
    }

    private fun findUserByToken(refreshToken: String): User {
        val userId = tokenProvider.getIdFromToken(refreshToken)

        return userRepository.findById(userId)
            .orElseThrow { BadCredentialsException(ExceptionMessage.INVALID_REFRESH_TOKEN.message) }
    }

    private fun rotateRefreshToken(user: User): String {
        val refreshToken = tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.role)
        user.refreshToken = refreshToken

        return refreshToken
    }
}
