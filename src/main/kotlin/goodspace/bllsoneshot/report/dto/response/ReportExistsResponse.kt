package goodspace.bllsoneshot.report.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject

data class ReportExistsResponse(
    val subject: Subject,
    val exists: Boolean
)
