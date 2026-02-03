package goodspace.bllsoneshot.task.dto.request

import goodspace.bllsoneshot.entity.assignment.Subject
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.util.Collections.emptyList

data class MentorTaskCreateRequest(
    @field:NotBlank(message = "멘티ID가 비어 있습니다.")
    val menteeId: Long,

    val subject: Subject,
    val startDate: LocalDate?,
    val dueDate: LocalDate?,
    @field:NotBlank(message = "할 일 이름이 비어 있습니다.")
    val taskName: String,
    @field:Positive(message = "목표 시간은 1분 이상이어야 합니다.")
    val goalMinutes: Int,

    val worksheets: List<WorksheetCreateRequest> = emptyList(),
    val columnLinks: List<ColumnLinkCreateRequest> = emptyList()
)

data class WorksheetCreateRequest(
    val fileId: Long?
)

data class ColumnLinkCreateRequest(
    val link: String?
)