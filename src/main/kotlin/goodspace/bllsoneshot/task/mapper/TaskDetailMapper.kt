package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.TaskDetailResponse
import org.springframework.stereotype.Component

@Component
class TaskDetailMapper(
    private val worksheetMapper: WorksheetMapper,
    private val columnLinkMapper: ColumnLinkMapper
) {

    fun map(task: Task): TaskDetailResponse {
        return TaskDetailResponse(
            taskId = task.id!!,
            taskName = task.name,
            createdBy = task.createdBy,
            subject = task.subject,
            goalMinutes = task.goalMinutes,
            actualMinutes = task.actualMinutes,
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }
}
