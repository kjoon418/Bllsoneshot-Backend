package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Comment
import goodspace.bllsoneshot.task.dto.response.feedback.QuestionResponse
import org.springframework.stereotype.Component

@Component
class QuestionMapper {

    fun map(comment: Comment): QuestionResponse {
        return QuestionResponse(
            questionId = comment.id!!,
            questionNumber = comment.commentAnnotation.number,
            content = comment.content,
            answer = comment.answer?.content
        )
    }
}
