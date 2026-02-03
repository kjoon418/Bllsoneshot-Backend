package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.CANNOT_COMPLETE_WITHOUT_ACTUAL_MINUTES
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.CascadeType
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentor: User?,

    @Column(nullable = false)
    val name: String,
    // TODO: 마감일, 생성일이 동시에 null일 수 없도록 검증
    val startDate: LocalDate?,
    val dueDate: LocalDate?,
        // 현재 목표 시간이 필수이므로 멘토가 할일 만들때도 반드시 목표시간을 설정해야함
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
    // Task 저장시 worksheets, columnLinks 도 DB에 저장하기 위해 CascadeType.PERSIST 추가, 삭제도 동일
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
