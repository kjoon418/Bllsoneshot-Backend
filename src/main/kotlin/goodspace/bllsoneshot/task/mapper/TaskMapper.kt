package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import org.springframework.stereotype.Component

@Component
class TaskMapper {

    fun map(task: Task): TaskResponse {
        return TaskResponse(
            taskId = task.id!!,
            taskName = task.name,

            createdBy = task.createdBy,
            subject = task.subject,
            generalComment = task.generalComment?.content,

            goalMinutes = task.goalMinutes,
            actualMinutes = task.actualMinutes,

            completed = task.completed,
            readFeedback = task.hasReadAllFeedbacks(),
            hasFeedback = task.hasFeedback(),
            hasWorksheet = task.hasWorkSheet(),
            hasProofShot = task.hasProofShot()
        )
    }

    fun map(tasks: List<Task>): List<TaskResponse> =
        tasks.map { map(it) }
}
