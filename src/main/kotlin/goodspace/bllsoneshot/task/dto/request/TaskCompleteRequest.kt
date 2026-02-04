package goodspace.bllsoneshot.task.dto.request

import java.time.LocalDate

data class TaskCompleteRequest(
    val currentDate: LocalDate,

    val actualMinutes: Int
)
