package goodspace.bllsoneshot.mentor.dto.request

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.task.dto.request.ColumnLinkCreateRequest
import goodspace.bllsoneshot.task.dto.request.WorksheetCreateRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class MentorTaskUpdateRequest(
    val subject: Subject,

    @field:NotBlank(message = "할 일 이름이 비어 있습니다.")
    @field:Size(max = 50, message = "할 일 이름은 50자를 초과할 수 없습니다.")
    val taskName: String,

    @field:PositiveOrZero(message = "목표 시간은 0 이상이어야 합니다.")
    val goalMinutes: Int,

    val worksheets: List<WorksheetCreateRequest> = emptyList(),
    val columnLinks: List<ColumnLinkCreateRequest> = emptyList()
)
