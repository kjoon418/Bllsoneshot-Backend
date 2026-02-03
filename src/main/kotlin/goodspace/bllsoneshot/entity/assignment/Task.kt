package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.CANNOT_COMPLETE_WITHOUT_ACTUAL_MINUTES
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

    @Column(nullable = false)
    val name: String,
    // TODO: 마감일, 생성일이 동시에 null일 수 없도록 검증
    val startDate: LocalDate?,
    val dueDate: LocalDate?,
    @Column(nullable = false)
    val goalMinutes: Int,
    val actualMinutes: Int?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val subject: Subject,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val createdBy: UserRole
) : BaseEntity() {
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val worksheets: MutableList<Worksheet> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val columnLinks: MutableList<ColumnLink> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val proofShots: MutableList<ProofShot> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val comments: MutableList<Comment> = mutableListOf()

    @OneToOne(fetch = FetchType.LAZY)
    val generalComment: GeneralComment? = null

    @Column(nullable = false)
    var completed: Boolean = false
        set(value) {
            if (value) {
                check(actualMinutes != null) { CANNOT_COMPLETE_WITHOUT_ACTUAL_MINUTES.message }
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
}
