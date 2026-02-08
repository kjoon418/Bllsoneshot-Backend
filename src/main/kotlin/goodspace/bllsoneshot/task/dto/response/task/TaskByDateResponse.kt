package goodspace.bllsoneshot.task.dto.response.task

import java.time.LocalDate

data class TaskByDateResponse(
    val date: LocalDate,
    val tasks: List<TaskResponse>,
)
