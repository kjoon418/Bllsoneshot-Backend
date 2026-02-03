package goodspace.bllsoneshot.task.dto.request

import goodspace.bllsoneshot.entity.assignment.Subject
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class MenteeTaskCreateRequest(
    val subject: Subject,

    val date: LocalDate,

    @field:NotBlank(message = "할 일 이름이 비어 있습니다.")
    val taskName: String,

    @field:Positive(message = "목표 시간은 1분 이상이어야 합니다.")
    val goalMinutes: Int,

    )
