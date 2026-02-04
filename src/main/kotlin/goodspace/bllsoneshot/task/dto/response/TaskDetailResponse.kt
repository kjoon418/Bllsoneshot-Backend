package goodspace.bllsoneshot.task.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse

data class TaskDetailResponse(
    val taskId: Long,
    val taskName: String,

    val createdBy: UserRole,
    val subject: Subject,
    val goalMinutes: Int,
    val actualMinutes: Int?,

    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>
)
