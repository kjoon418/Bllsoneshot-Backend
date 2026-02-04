package goodspace.bllsoneshot.mypage.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.mypage.dto.response.LearningHistoryResponse
import goodspace.bllsoneshot.task.mapper.TaskMapper
import org.springframework.stereotype.Component

@Component
class LearningHistoryMapper(
    private val taskMapper: TaskMapper
) {

    fun map(
        todayTasks: List<Task>,
        historyTasks: List<Task>
    ): LearningHistoryResponse {
        return LearningHistoryResponse(
            todayTasks = taskMapper.map(todayTasks),
            historyTasks = taskMapper.map(historyTasks)
        )
    }
}
