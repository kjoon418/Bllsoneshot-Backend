package goodspace.bllsoneshot.report.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import java.time.LocalDate

data class ReportResponse(
    val reportId: Long,
    val subject: Subject,

    val startDate: LocalDate,
    val endDate: LocalDate,

    val generalComment: String,
    val goodPoints: List<String>,
    val badPoints: List<String>
)
