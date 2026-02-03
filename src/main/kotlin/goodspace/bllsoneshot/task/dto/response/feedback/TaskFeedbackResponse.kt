package goodspace.bllsoneshot.task.dto.response.feedback

import goodspace.bllsoneshot.entity.assignment.Subject

data class TaskFeedbackResponse(
    val taskId: Long,
    val subject: Subject,
    val taskName: String,

    val mentorName: String,
    val generalComment: String,

    val proofShots: List<ProofShotResponse>,
    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>
)
