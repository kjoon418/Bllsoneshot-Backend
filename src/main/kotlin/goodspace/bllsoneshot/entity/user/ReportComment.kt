package goodspace.bllsoneshot.entity.user

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.global.exception.ExceptionMessage.REPORT_CONTENT_CANNOT_BLANK
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class ReportComment(
    @ManyToOne(fetch = FetchType.LAZY)
    val report: LearningReport,

    @Column(nullable = false)
    val content: String
) : BaseEntity() {

    init {
        validateContent()
    }

    private fun validateContent() {
        check(content.isNotBlank()) { REPORT_CONTENT_CANNOT_BLANK.message }
    }
}
