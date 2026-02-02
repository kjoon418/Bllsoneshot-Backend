package goodspace.bllsoneshot.entity.assignment

import jakarta.persistence.*

@Entity
class Feedback(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val assignment: Assignment
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
