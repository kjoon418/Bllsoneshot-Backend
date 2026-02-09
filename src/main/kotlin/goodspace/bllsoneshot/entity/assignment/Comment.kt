package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Transient

@Entity
class Comment(
    @ManyToOne(fetch = FetchType.LAZY)
    val proofShot: ProofShot,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val annotation: CommentAnnotation,

    @Column(nullable = false)
    val content: String,
    @Column(nullable = false)
    val starred: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CommentType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val registerStatus: RegisterStatus = RegisterStatus.CONFIRMED
) : BaseEntity() {

    init {
        // 멘티가 작성한 질문은 바로 읽음 처리
        if (isQuestion) {
            markAsRead()
        }
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var answer: Answer? = null

    private var readByMentee: Boolean = false

    @get:Transient
    val isQuestion: Boolean
        get() = type == CommentType.QUESTION

    @get:Transient
    val isFeedback: Boolean
        get() = type == CommentType.FEEDBACK

    @get:Transient
    val isConfirmed: Boolean
        get() = registerStatus == RegisterStatus.CONFIRMED

    @get:Transient
    val isTemporary: Boolean
        get() = registerStatus == RegisterStatus.TEMPORARY

    @get:Transient
    val isRead: Boolean
        get() = readByMentee

    fun markAsRead() {
        readByMentee = true
    }
}
