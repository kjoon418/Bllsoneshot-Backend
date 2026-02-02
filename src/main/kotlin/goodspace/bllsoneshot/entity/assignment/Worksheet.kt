package goodspace.bllsoneshot.entity.assignment

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Worksheet(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val assignment: Assignment
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
