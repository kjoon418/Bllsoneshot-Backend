package goodspace.bllsoneshot.task.dto.response.task

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse
import java.time.LocalDate

data class TaskDetailResponse(
    val taskId: Long,
    val taskName: String,

    val createdBy: UserRole,
    val subject: Subject,
    val goalMinutes: Int,
    val actualMinutes: Int?,

    val isResource: Boolean,
    val uploadedAt: LocalDate?,

    val hasFeedback: Boolean,
    val generalComment: String?,
    val mentorName: String,

    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>
)
