package goodspace.bllsoneshot.repository.task

import goodspace.bllsoneshot.entity.assignment.Subject
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
    fun findCurrentTasks(userId: Long, date: LocalDate): List<Task>

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :userId
        AND (t.startDate IS NULL OR :date >= t.startDate)
        AND t.dueDate IS NOT NULL AND :date <= t.dueDate
        """
    )
    fun findCurrentTasksDueDateExists(userId: Long, date: LocalDate): List<Task>

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :userId AND t.subject = :subject
        AND (t.dueDate IS NULL OR t.dueDate < :date)
        """
    )
    fun findPreviousTasks(userId: Long, subject: Subject, date: LocalDate): List<Task>

    @Query(
        """
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.mentee m
        LEFT JOIN FETCH m.mentor
        LEFT JOIN FETCH t.generalComment
        LEFT JOIN FETCH t.proofShots
        WHERE t.id = :taskId
        """
    )
    fun findByIdWithMenteeAndGeneralCommentAndProofShots(taskId: Long): Task?
}
