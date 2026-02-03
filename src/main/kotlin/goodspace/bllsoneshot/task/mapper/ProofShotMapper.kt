package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.ProofShot
import goodspace.bllsoneshot.task.dto.response.feedback.ProofShotResponse
import org.springframework.stereotype.Component

@Component
class ProofShotMapper(
    private val questionMapper: QuestionMapper,
    private val feedbackMapper: FeedbackMapper,
    private val commentAnnotationMapper: CommentAnnotationMapper
) {

    fun map(proofShot: ProofShot): ProofShotResponse {
        val questions = proofShot.questComments.sortedBy { it.commentAnnotation.number }
        val feedbacks = proofShot.registeredFeedbackComments.sortedBy { it.commentAnnotation.number }

        return ProofShotResponse(
            proofShotId = proofShot.id!!,
            imageFileId = proofShot.file.id!!,
            questions = questions.map { questionMapper.map(it) },
            questionAnnotations = questions.map { commentAnnotationMapper.map(it.commentAnnotation) },
            feedbacks = feedbacks.map { feedbackMapper.map(it) },
            feedbackAnnotations = feedbacks.map { commentAnnotationMapper.map(it.commentAnnotation) }
        )
    }
}
