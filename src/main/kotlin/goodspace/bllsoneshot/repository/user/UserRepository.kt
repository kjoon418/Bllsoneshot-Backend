package goodspace.bllsoneshot.repository.user

import goodspace.bllsoneshot.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByLoginId(loginId: String): Boolean
    fun findByLoginId(loginId: String): User?
}
