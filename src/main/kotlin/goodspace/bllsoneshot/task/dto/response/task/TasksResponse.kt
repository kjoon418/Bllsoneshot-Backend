package goodspace.bllsoneshot.task.dto.response.task

data class TasksResponse(
    val completedTaskAmount: Int,
    val taskAmount: Int,

    val goalMinutesTotal: Int,
    val actualMinutesTotal: Int,

    val tasks: List<TaskResponse>
)
