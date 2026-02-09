package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.NEGATIVE_ACTUAL_MINUTES
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import java.time.LocalDate

@Entity
class Task(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentee: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var subject: Subject,

    val date: LocalDate? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var goalMinutes: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val createdBy: UserRole,

    @Column(nullable = false)
    val isResource: Boolean = false
) : BaseEntity() {
    // Task 저장시 연관 엔티티도 함게 저장, 삭제되도록 cascade 및 orphanRemoval 설정
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val worksheets: MutableList<Worksheet> = mutableListOf()

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val columnLinks: MutableList<ColumnLink> = mutableListOf()

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val proofShots: MutableList<ProofShot> = mutableListOf()

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var generalComment: GeneralComment? = null

    @Column(nullable = false)
    var completed: Boolean = false

    @get:Transient
    val questions: List<Comment>
        get() =  proofShots.flatMap { it.questComments }

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
        proofShots.any { it.hasFeedback() }

    fun hasReadAllFeedbacks() =
        proofShots.all { it.hasReadAllFeedbacks() }

    fun markFeedbackAsRead() {
        proofShots.forEach { it.markFeedbackAsRead() }
    }

    // TODO: 로직 이해하기
    //  피드백 재저장 시, 기존 FEEDBACK 타입 Comment만 삭제하고 QUESTION은 보존한다.
    //  Comment는 task.comments와 proofShot.comments 양쪽에 참조되어 있으므로,
    //  orphanRemoval이 정상 동작하려면 반드시 양쪽 컬렉션에서 모두 제거해야 한다.
    fun clearFeedbackComments() {
        proofShots.forEach { ps ->
            ps.comments.removeIf { it.isFeedback }
        }
    }

    fun clearTemporaryFeedbackComments() {
        proofShots.forEach { it.clearTemporaryFeedbackComments() }
    }

    fun clearTemporaryAnswers() {
        proofShots.forEach { it.clearTemporaryAnswers() }
    }

    companion object {
        private const val MINIMUM_ACTUAL_MINUTES = 0
    }
}
