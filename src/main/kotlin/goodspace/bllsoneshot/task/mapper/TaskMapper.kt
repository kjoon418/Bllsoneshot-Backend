package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.task.TaskAmountResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskByDateResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskResponse
import org.springframework.stereotype.Component

@Component
class TaskMapper {

    fun map(task: Task): TaskResponse {
        return TaskResponse(
            taskId = task.id!!,
            taskSubject = task.subject,
            taskDate = task.date,
            taskName = task.name,
            createdBy = task.createdBy,
            completed = task.completed,
            readFeedback = task.hasReadAllFeedbacks(),
            hasFeedback = task.hasFeedback(),
            hasWorksheet = task.hasWorkSheet(),
            hasProofShot = task.hasProofShot()
        )
    }

    fun map(tasks: List<Task>): List<TaskResponse> =
        tasks.map { map(it) }

    fun mapByDate(tasks: List<Task>): List<TaskByDateResponse> {
        return tasks
            .groupBy { it.date!! }
            .toSortedMap()
            .map { (date, taskList) ->
                TaskByDateResponse(date = date, tasks = map(taskList))
            }
    }

    fun mapToTaskAmounts(tasks: List<Task>): List<TaskAmountResponse> {
        return tasks
            .groupBy { it.date!! }
            .toSortedMap()
            .map { (date, taskList) ->
                TaskAmountResponse(date = date, taskAmount = taskList.size)
            }
    }
}
