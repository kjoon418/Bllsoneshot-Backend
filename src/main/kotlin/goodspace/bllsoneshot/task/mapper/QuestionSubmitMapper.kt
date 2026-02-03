package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Comment
import goodspace.bllsoneshot.task.dto.response.QuestionSubmitResponse
import org.springframework.stereotype.Component

@Component
class QuestionSubmitMapper {

    fun map(comment: Comment): QuestionSubmitResponse {
        val annotation = comment.commentAnnotation

        return QuestionSubmitResponse(
            number = annotation.number,
            content = comment.content,
            percentX = annotation.percentX,
            percentY = annotation.percentY
        )
    }
}
