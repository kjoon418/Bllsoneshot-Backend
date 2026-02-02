package goodspace.bllsoneshot.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @ManyToOne(fetch = FetchType.LAZY)
    val mentor: User? = null,

    val role: UserRole
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
