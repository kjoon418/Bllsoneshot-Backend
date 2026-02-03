package goodspace.bllsoneshot.task.dto.response.feedback

data class CommentAnnotationResponse(
    val annotationId: Long,
    val annotationNumber: Int,

    val xPercent: Double,
    val yPercent: Double
)
