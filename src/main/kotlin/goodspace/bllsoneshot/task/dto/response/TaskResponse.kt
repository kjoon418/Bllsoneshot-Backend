package goodspace.bllsoneshot.task.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.UserRole

data class TaskResponse(
    val taskId: Long,
    val taskName: String,

    val createdBy: UserRole,
    val subject: Subject,
    val generalComment: String?,

    val goalMinutes: Int,
    val actualMinutes: Int?,

    val completed: Boolean,
    val readFeedback: Boolean,
    val hasFeedback: Boolean,
    val hasWorksheet: Boolean,
    val hasProofShot: Boolean
)
