package goodspace.bllsoneshot.mypage.service

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.mypage.dto.response.LearningHistoryResponse
import goodspace.bllsoneshot.mypage.dto.response.LearningStatusResponse
import goodspace.bllsoneshot.mypage.mapper.LearningHistoryMapper
import goodspace.bllsoneshot.mypage.mapper.LearningStatusMapper
import goodspace.bllsoneshot.repository.task.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MyPageService(
    private val taskRepository: TaskRepository,
    private val learningStatusMapper: LearningStatusMapper,
    private val learningHistoryMapper: LearningHistoryMapper
) {

    @Transactional(readOnly = true)
    fun getTotalLearningStatus(userId: Long, date: LocalDate): List<LearningStatusResponse> {
        val tasks = taskRepository.findCurrentTasks(userId, date)

        return Subject.entries
            .map { learningStatusMapper.map(it, tasks) }
    }

    @Transactional(readOnly = true)
    fun getLearningStatusBySubject(userId: Long, subject: Subject, date: LocalDate): LearningHistoryResponse {
        val todayTasks = taskRepository.findCurrentTasksDueDateExists(userId, date)
            .filter { it.subject == subject }
            .sortedWith(compareBy<Task> { it.completed }.thenByDescending { it.id })

        val historyTasks = taskRepository.findPreviousTasks(userId, subject, date)
            .sortedWith(compareBy<Task> { it.completed }.thenByDescending { it.id })

        return learningHistoryMapper.map(todayTasks, historyTasks)
    }
}
