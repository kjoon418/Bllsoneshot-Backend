package goodspace.bllsoneshot.mypage.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.mypage.dto.response.LearningStatusResponse
import org.springframework.stereotype.Component

@Component
class LearningStatusMapper {

    fun map(
        subject: Subject,
        tasks: List<Task>
    ): LearningStatusResponse {
        val subjectTasks = tasks.filter { it.subject == subject }

        return LearningStatusResponse(
            subject = subject,
            taskAmount = subjectTasks.size,
            completedTaskAmount = subjectTasks.count { it.completed }
        )
    }
}
