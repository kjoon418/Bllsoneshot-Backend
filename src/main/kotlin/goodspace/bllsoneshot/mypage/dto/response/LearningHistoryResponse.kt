package goodspace.bllsoneshot.mypage.dto.response

import goodspace.bllsoneshot.task.dto.response.task.TaskResponse

data class LearningHistoryResponse(
    val todayTasks: List<TaskResponse>,
    val historyTasks: List<TaskResponse>
)
