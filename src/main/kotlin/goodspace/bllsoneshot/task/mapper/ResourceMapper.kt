package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.ResourceResponse
import org.springframework.stereotype.Component

@Component
class ResourceMapper(
    private val worksheetMapper: WorksheetMapper,
    private val columnLinkMapper: ColumnLinkMapper
) {

    fun map(task: Task): ResourceResponse {
        return ResourceResponse(
            resourceId = task.id!!,
            subject = task.subject,
            resourceName = task.name,
            registeredDate = task.date ?: java.time.LocalDate.now(),
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }

    fun map(tasks: List<Task>): List<ResourceResponse> =
        tasks.map { map(it) }
}
