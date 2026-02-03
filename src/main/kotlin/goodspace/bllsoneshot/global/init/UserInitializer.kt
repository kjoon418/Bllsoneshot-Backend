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
import org.springframework.transaction.annotation.Transactional

@Component
class UserInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,

    @Value("\${init.mentor.login-id}")
    private val mentorLoginId: String,
    @Value("\${init.mentor.password}")
    private val mentorPassword: String,
    @Value("\${init.mentor.name}")
    private val mentorName: String,

    @Value("\${init.mentee1.login-id}")
    private val mentee1LoginId: String,
    @Value("\${init.mentee1.password}")
    private val mentee1Password: String,
    @Value("\${init.mentee1.name}")
    private val mentee1Name: String,

    @Value("\${init.mentee2.login-id}")
    private val mentee2LoginId: String,
    @Value("\${init.mentee2.password}")
    private val mentee2Password: String,
    @Value("\${init.mentee2.name}")
    private val mentee2Name: String
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        val mentor = initIfNotExists(mentorLoginId, mentorPassword, mentorName, ROLE_MENTOR)

        initIfNotExists(mentee1LoginId, mentee1Password, mentee1Name, ROLE_MENTEE, mentor)
        initIfNotExists(mentee2LoginId, mentee2Password, mentee2Name, ROLE_MENTEE, mentor)
    }

    private fun initIfNotExists(
        loginId: String,
        password: String,
        name: String,
        role: UserRole,
        mentor: User? = null
    ): User? {
        if (userRepository.existsByLoginId(loginId)) {
            return null
        }

        val user = User(
            loginId = loginId,
            password = passwordEncoder.encode(password)!!,
            role = role,
            name = name,
            mentor = mentor
        )

        return userRepository.save(user)
    }
}
