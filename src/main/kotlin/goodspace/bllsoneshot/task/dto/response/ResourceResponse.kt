package goodspace.bllsoneshot.task.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse
import java.time.LocalDate

data class ResourceResponse(
    val resourceId: Long,
    val subject: Subject,
    val resourceName: String,
    val registeredDate: LocalDate,
    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>
)
