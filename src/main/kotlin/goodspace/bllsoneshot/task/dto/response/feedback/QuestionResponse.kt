package goodspace.bllsoneshot.task.dto.response.feedback

data class QuestionResponse(
    val questionId: Long,
    val questionNumber: Int,

    val content: String,
    val answer: String?,

    val annotation: CommentAnnotationResponse
)
