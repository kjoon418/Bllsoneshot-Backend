package goodspace.bllsoneshot.report.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject

data class ReportExistResponse(
    val subjects: List<Subject>
)
