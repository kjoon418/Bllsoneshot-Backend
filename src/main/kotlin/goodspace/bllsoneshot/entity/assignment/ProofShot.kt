package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Transient

@Entity
class ProofShot(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val file: File
) : BaseEntity() {
    @OneToMany(mappedBy = "proofShot", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val comments: MutableList<Comment> = mutableListOf()

    @get:Transient
    val questComments: List<Comment>
        get() = comments.filter { it.isQuestion }

    @get:Transient
    val confirmedFeedbackComments: List<Comment>
        get() = comments.filter { it.isFeedback && it.isConfirmed }

    @get:Transient
    val temporaryFeedbackComments: List<Comment>
        get() = comments.filter { it.isFeedback && it.isTemporary }

    fun hasFeedback(): Boolean =
        comments.any { it.isFeedback && it.isConfirmed }

    fun hasReadAllFeedbacks(): Boolean {
        if (!hasFeedback()) {
            return true
        }

        return comments.filter { it.isFeedback && it.isConfirmed }
            .all { it.isRead }
    }

    fun markFeedbackAsRead() {
        comments.forEach { it.markAsRead() }
    }

    fun clearTemporaryFeedbackComments() {
        comments.removeIf { it.isFeedback && it.isTemporary }
    }

    fun clearTemporaryAnswers() {
        comments.filter { it.isQuestion && it.answer != null }
            .forEach { question ->
                question.answer?.temporaryContent = null
            }
    }
}
