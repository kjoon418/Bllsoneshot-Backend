package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import org.springframework.stereotype.Component

@Component
class TaskFeedbackMapper(
    private val proofShotMapper: ProofShotMapper
) {

    fun map(task: Task): TaskFeedbackResponse {
        return TaskFeedbackResponse(
            taskId = task.id!!,
            subject = task.subject,
            taskName = task.name,
            mentorName = task.mentee.mentor?.name ?: "",
            generalComment = task.generalComment?.content ?: "",
            proofShots = task.proofShots.map { proofShotMapper.map(it) }
        )
    }
}
