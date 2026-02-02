package goodspace.bllsoneshot.auth.service

import goodspace.bllsoneshot.auth.dto.LoginRequest
import goodspace.bllsoneshot.auth.dto.LoginResult
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.global.security.TokenProvider
import goodspace.bllsoneshot.global.security.TokenType
import goodspace.bllsoneshot.repository.user.UserRepository
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
        val user = findUserBy(request.loginId)
        validatePassword(request.password, user.password)

        val userId = user.id!!
        val accessToken = tokenProvider.createToken(userId, TokenType.ACCESS, user.role)
        val refreshToken = tokenProvider.createToken(userId, TokenType.REFRESH, user.role)

        user.refreshToken = refreshToken

        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun findUserBy(loginId: String): User {
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
}
