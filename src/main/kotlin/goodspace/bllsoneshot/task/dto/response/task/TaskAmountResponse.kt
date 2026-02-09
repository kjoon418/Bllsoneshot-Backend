package goodspace.bllsoneshot.task.dto.response.task

import java.time.LocalDate

data class TaskAmountResponse(
    val date: LocalDate,
    val taskAmount: Int
)
