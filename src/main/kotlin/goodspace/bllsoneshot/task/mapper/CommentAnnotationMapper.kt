package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.CommentAnnotation
import goodspace.bllsoneshot.task.dto.response.feedback.CommentAnnotationResponse
import org.springframework.stereotype.Component

@Component
class CommentAnnotationMapper {

    fun map(annotation: CommentAnnotation): CommentAnnotationResponse {
        return CommentAnnotationResponse(
            annotationId = annotation.id!!,
            annotationNumber = annotation.number,
            percentX = annotation.percentX,
            percentY = annotation.percentY
        )
    }
}
