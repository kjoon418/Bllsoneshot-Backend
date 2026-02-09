package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.ProofShot
import goodspace.bllsoneshot.task.dto.response.feedback.ProofShotResponse
import org.springframework.stereotype.Component

@Component
class ProofShotMapper(
    private val questionMapper: QuestionMapper,
    private val feedbackMapper: FeedbackMapper
) {

    fun map(proofShot: ProofShot): ProofShotResponse {
        val questions = proofShot.questComments.sortedBy { it.annotation.number }
        val feedbacks = proofShot.confirmedFeedbackComments.sortedBy { it.annotation.number }

        return ProofShotResponse(
            proofShotId = proofShot.id!!,
            imageFileId = proofShot.file.id!!,
            questions = questions.map { questionMapper.mapConfirmed(it) },
            feedbacks = feedbacks.map { feedbackMapper.map(it) }
        )
    }
}
