package goodspace.bllsoneshot.task.service

import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.mapper.TaskMapper
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class MenteeTaskService(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper
) {

    fun findTasksByDate(
        userId: Long,
        date: LocalDate
    ): List<TaskResponse> {
        val tasks = taskRepository.findByMenteeIdAndDate(userId, date)

        return taskMapper.map(tasks)
    }
}
