package goodspace.bllsoneshot.task.dto.response

import java.time.LocalDate

data class TaskByDateResponse(
    val date: LocalDate,
    val tasks: List<TaskResponse>,
)
