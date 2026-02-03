package goodspace.bllsoneshot.task.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject

data class TaskSubmitResponse(
    val taskId: Long,
    val name: String,
    val subject: Subject,
    val proofShots: List<ProofShotSubmitResponse>
)
