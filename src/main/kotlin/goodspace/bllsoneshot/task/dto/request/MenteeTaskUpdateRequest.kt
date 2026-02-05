package goodspace.bllsoneshot.task.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class MenteeTaskUpdateRequest(
    @field:NotBlank(message = "할 일 이름이 비어 있습니다.")
    val taskName: String,

    @field:PositiveOrZero(message = "목표 시간은 0 이상이어야 합니다.")
    val goalMinutes: Int
)
