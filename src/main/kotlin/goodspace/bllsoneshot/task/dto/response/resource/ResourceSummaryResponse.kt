package goodspace.bllsoneshot.task.dto.response.resource

import goodspace.bllsoneshot.entity.assignment.Subject
import java.time.LocalDate

data class ResourceSummaryResponse(
    val resourceId: Long,
    val subject: Subject,
    val resourceName: String,
    val registeredDate: LocalDate
)