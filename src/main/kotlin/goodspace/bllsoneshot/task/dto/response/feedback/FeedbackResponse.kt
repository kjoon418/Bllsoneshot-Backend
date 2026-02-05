package goodspace.bllsoneshot.task.dto.response.feedback

import goodspace.bllsoneshot.entity.assignment.RegisterStatus

data class FeedbackResponse(
    val feedbackId: Long,
    val feedbackNumber: Int,

    val content: String,
    val starred: Boolean,
    val registerStatus: RegisterStatus,

    val annotation: CommentAnnotationResponse
)
