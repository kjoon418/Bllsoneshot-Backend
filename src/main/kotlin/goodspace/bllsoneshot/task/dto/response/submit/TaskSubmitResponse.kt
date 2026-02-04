package goodspace.bllsoneshot.task.dto.response.submit

import goodspace.bllsoneshot.entity.assignment.Subject

data class TaskSubmitResponse(
    val taskId: Long,
    val name: String,
    val subject: Subject,
    val proofShots: List<ProofShotSubmitResponse>
)
