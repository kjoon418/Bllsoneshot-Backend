package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.NEGATIVE_ACTUAL_MINUTES
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import java.time.LocalDate

@Entity
class Task(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentee: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val subject: Subject,

    val startDate: LocalDate?,
    val dueDate: LocalDate?,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val goalMinutes: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val createdBy: UserRole
) : BaseEntity() {
    // Task 저장시 연관 엔티티도 함게 저장, 삭제되도록 cascade 및 orphanRemoval 설정
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val worksheets: MutableList<Worksheet> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val columnLinks: MutableList<ColumnLink> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val proofShots: MutableList<ProofShot> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val comments: MutableList<Comment> = mutableListOf()

    @OneToOne(fetch = FetchType.LAZY)
    val generalComment: GeneralComment? = null

    @Column(nullable = false)
    var completed: Boolean = false

    var actualMinutes: Int? = null
        set(value) {
            if (value != null && value < MINIMUM_ACTUAL_MINUTES) {
                throw IllegalArgumentException(NEGATIVE_ACTUAL_MINUTES.message)
            }

            field = value
        }

    fun hasWorkSheet(): Boolean =
        worksheets.isNotEmpty()

    fun hasProofShot(): Boolean =
        proofShots.isNotEmpty()

    fun hasFeedback(): Boolean =
        comments.any { it.isFeedback && it.isRegistered }

    fun hasReadAllFeedbacks(): Boolean {
        if (!hasFeedback()) {
            return true
        }

        return comments.filter { it.isFeedback && it.isRegistered }
            .all { it.isRead }
    }

    fun markFeedbackAsRead() {
        comments.forEach { it.markAsRead() }
    }

    companion object {
        private const val MINIMUM_ACTUAL_MINUTES = 0
    }
}
