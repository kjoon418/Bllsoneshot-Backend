package goodspace.bllsoneshot.entity.counseling

import goodspace.bllsoneshot.entity.user.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Counseling(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentor: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentee: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
