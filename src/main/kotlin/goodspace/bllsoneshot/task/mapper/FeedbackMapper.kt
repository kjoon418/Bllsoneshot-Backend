package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Comment
import goodspace.bllsoneshot.task.dto.response.feedback.FeedbackResponse
import org.springframework.stereotype.Component

@Component
class FeedbackMapper(
    private val commentAnnotationMapper: CommentAnnotationMapper
) {

    fun map(comment: Comment): FeedbackResponse {
        return FeedbackResponse(
            feedbackId = comment.id!!,
            feedbackNumber = comment.commentAnnotation.number,
            content = comment.content,
            starred = comment.starred,
            registerStatus = comment.registerStatus,
            annotation = commentAnnotationMapper.map(comment.commentAnnotation)
        )
    }
}
