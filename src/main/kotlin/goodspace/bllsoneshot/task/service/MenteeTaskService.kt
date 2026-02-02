package goodspace.bllsoneshot.task.service

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.task.dto.request.MenteeTaskCreateRequest
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.mapper.TaskMapper
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenteeTaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val taskMapper: TaskMapper
) {

    fun findTasksByDate(
        userId: Long,
        date: LocalDate
    ): List<TaskResponse> {
        val tasks = taskRepository.findByMenteeIdAndDate(userId, date)

        return taskMapper.map(tasks)
    }

    @Transactional
    fun createTask(userId: Long, request: MenteeTaskCreateRequest): TaskResponse {
        val mentee = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(ExceptionMessage.USER_NOT_FOUND.message) }

        val task = Task(
            mentee = mentee,
            name = request.taskName,
            startDate = request.date,
            dueDate = request.date,
            goalMinutes = request.goalMinutes,
            actualMinutes = null,
            subject = request.subject,
            createdBy = UserRole.ROLE_MENTEE
        )

        val savedTask = taskRepository.save(task)

        return taskMapper.map(savedTask)
    }
}
