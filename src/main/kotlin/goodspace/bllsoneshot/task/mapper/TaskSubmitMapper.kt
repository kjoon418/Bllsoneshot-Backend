package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.submit.TaskSubmitResponse
import org.springframework.stereotype.Component

@Component
class TaskSubmitMapper(
    private val proofShotSubmitMapper: ProofShotSubmitMapper
) {

    fun map(task: Task): TaskSubmitResponse {
        return TaskSubmitResponse(
            taskId = task.id!!,
            name = task.name,
            subject = task.subject,
            proofShots = task.proofShots.map { proofShotSubmitMapper.map(it) }
        )
    }
}
