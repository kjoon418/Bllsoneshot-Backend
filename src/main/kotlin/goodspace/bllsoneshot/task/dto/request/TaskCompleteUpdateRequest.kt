package goodspace.bllsoneshot.task.dto.request

import java.time.LocalDate

data class TaskCompleteUpdateRequest(
    val completed: Boolean,
    val currentDate: LocalDate
)
