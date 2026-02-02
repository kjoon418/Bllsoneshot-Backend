package goodspace.bllsoneshot.repository.task

import goodspace.bllsoneshot.entity.assignment.Task
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<Task, Long> {

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :userId
        AND (t.startDate IS NULL OR :date >= t.startDate)
        AND (t.dueDate IS NULL OR :date <= t.dueDate)
        """
    )
    fun findByMenteeIdAndDate(userId: Long, date: LocalDate): List<Task>
}
