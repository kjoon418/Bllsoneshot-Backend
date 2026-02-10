package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskFormResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskDetailResponse
import org.springframework.stereotype.Component

@Component
class TaskDetailMapper(
    private val worksheetMapper: WorksheetMapper,
    private val columnLinkMapper: ColumnLinkMapper
) {

    fun mapToForm(task: Task): MentorTaskFormResponse {
        return MentorTaskFormResponse(
            subject = task.subject,
            dates = listOfNotNull(task.date),
            taskNames = listOf(task.name),
            goalMinutes = task.goalMinutes,
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }

    fun map(task: Task): TaskDetailResponse {
        return TaskDetailResponse(
            taskId = task.id!!,
            taskName = task.name,
            createdBy = task.createdBy,
            subject = task.subject,
            goalMinutes = task.goalMinutes,
            actualMinutes = task.actualMinutes,
            hasFeedback = task.hasFeedback(),
            generalComment = task.generalComment?.content,
            mentorName = task.mentee.mentor?.name ?: "",
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }
}
