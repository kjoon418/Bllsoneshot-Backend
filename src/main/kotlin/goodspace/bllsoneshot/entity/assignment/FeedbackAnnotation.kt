package goodspace.bllsoneshot.entity.assignment

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class FeedbackAnnotation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val feedback: Feedback,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val proofShot: ProofShot
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
