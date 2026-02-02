package goodspace.bllsoneshot.global.init

import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.entity.user.UserRole.ROLE_MENTEE
import goodspace.bllsoneshot.entity.user.UserRole.ROLE_MENTOR
import goodspace.bllsoneshot.repository.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,

    @Value("\${init.mentor.login-id:mentor}")
    private val mentorLoginId: String,
    @Value("\${init.mentor.password:mentor123}")
    private val mentorPassword: String,
    @Value("\${init.mentee1.login-id:mentee1}")
    private val mentee1LoginId: String,
    @Value("\${init.mentee1.password:mentee1}")
    private val mentee1Password: String,
    @Value("\${init.mentee2.login-id:mentee2}")
    private val mentee2LoginId: String,
    @Value("\${init.mentee2.password:mentee2}")
    private val mentee2Password: String
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        initIfNotExists(mentorLoginId, mentorPassword, ROLE_MENTOR)
        initIfNotExists(mentee1LoginId, mentee1Password, ROLE_MENTEE)
        initIfNotExists(mentee2LoginId, mentee2Password, ROLE_MENTEE)
    }

    private fun initIfNotExists(
        loginId: String,
        password: String,
        role: UserRole
    ) {
        if (userRepository.existsByLoginId(loginId)) {
            return
        }

        val user = User(
            loginId = loginId,
            password = passwordEncoder.encode(password)!!,
            role = role
        )
        userRepository.save(user)
    }
}
