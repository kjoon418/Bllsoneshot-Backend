package goodspace.bllsoneshot.entity.user

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.assignment.GeneralComment
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.global.exception.ExceptionMessage.REPORT_CONTENT_REQUIRED
import goodspace.bllsoneshot.global.exception.ExceptionMessage.REPORT_DATE_INVALID
import goodspace.bllsoneshot.global.exception.ExceptionMessage.REPORT_SUBJECT_INVALID
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
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["mentee_id", "subject", "start_date", "end_date"])
    ]
)
class LearningReport(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val mentee: User,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(nullable = false)
    val generalComment: GeneralComment,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val subject: Subject,
    @Column(nullable = false)
    val startDate: LocalDate,
    @Column(nullable = false)
    val endDate: LocalDate,

    goodPointContents: List<String>,
    badPointContents: List<String>
) : BaseEntity() {
    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val goodPoints: MutableList<ReportComment> = mutableListOf()

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val badPoints: MutableList<ReportComment> = mutableListOf()

    init {
        validateSubject()
        validateDate()

        goodPoints.addAll(goodPointContents.map { ReportComment(this, it.trim()) })
        badPoints.addAll(badPointContents.map { ReportComment(this, it.trim()) })
        validatePointSize()
    }

    private fun validateSubject() {
        check(subject in Subject.entriesExcludeResource()) { REPORT_SUBJECT_INVALID.message }
    }

    private fun validateDate() {
        check(!startDate.isAfter(endDate)) { REPORT_DATE_INVALID.message }
    }

    private fun validatePointSize() {
        check(goodPoints.isNotEmpty() && badPoints.isNotEmpty()) { REPORT_CONTENT_REQUIRED.message }
    }
}
