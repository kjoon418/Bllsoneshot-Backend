package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.task.dto.response.submit.ResourceResponse
import goodspace.bllsoneshot.task.dto.response.resource.ResourceSummaryResponse
import org.springframework.stereotype.Component
import java.time.LocalDate

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
            registeredDate = task.date ?: LocalDate.now(),
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }

    fun mapToSummary(task: Task): ResourceSummaryResponse {
        return ResourceSummaryResponse(
            resourceId = task.id!!,
            subject = task.subject,
            resourceName = task.name,
            registeredDate = task.date ?: LocalDate.now()
        )
    }

    fun mapToSummaries(tasks: List<Task>): List<ResourceSummaryResponse> =
        tasks.map { mapToSummary(it) }
}
