package goodspace.bllsoneshot.task.dto.response.task

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.UserRole
import java.time.LocalDate

data class TaskResponse(
    val taskId: Long,
    val taskSubject: Subject,
    val taskDate: LocalDate?,
    val taskName: String,
    val createdBy: UserRole,
    val completed: Boolean,
    val readFeedback: Boolean,
    val hasFeedback: Boolean,
    val hasWorksheet: Boolean,
    val hasProofShot: Boolean,
    val isResources: Boolean
)
