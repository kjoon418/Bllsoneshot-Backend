package goodspace.bllsoneshot.mentor.dto.request

data class MentorFeedbackRequest(
    val generalComment: String?,
    val proofShotFeedbacks: List<ProofShotFeedbackRequest> = emptyList(),
    val questionAnswers: List<QuestionAnswerRequest> = emptyList()
)

data class ProofShotFeedbackRequest(
    val proofShotId: Long,
    val feedbacks: List<DetailFeedbackRequest>
)

data class DetailFeedbackRequest(
    val content: String,
    val starred: Boolean = false,
    val percentX: Double,
    val percentY: Double
)

data class QuestionAnswerRequest(
    val questionId: Long,
    val content: String
)
