package goodspace.bllsoneshot.mentor.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse
import java.time.LocalDate

data class MentorTaskEditResponse(
    val subject: Subject,
    val date: LocalDate?,
    val taskName: String,
    val goalMinutes: Int,
    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>,
    val completed: Boolean,
)
