package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.task.TasksResponse
import org.springframework.stereotype.Component

@Component
class TasksMapper(
    private val taskMapper: TaskMapper
) {

    fun map(tasks: List<Task>): TasksResponse {
        return TasksResponse(
            completedTaskAmount = tasks.count { it.completed },
            taskAmount = tasks.size,
            goalMinutesTotal = tasks.sumOf { it.goalMinutes },
            actualMinutesTotal = tasks.sumOf { it.actualMinutes ?: 0 },
            tasks = taskMapper.map(tasks)
        )
    }
}
