package goodspace.bllsoneshot.mentor.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.task.dto.response.feedback.ProofShotResponse

data class MentorTaskDetailResponse(
    val taskId: Long,
    val mentorName: String,
    val taskName: String,
    val menteeName: String,
    val proofShots: List<ProofShotResponse>,
    val generalComment: String?,

    val subject: Subject,
)
