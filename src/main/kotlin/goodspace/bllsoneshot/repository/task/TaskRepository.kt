package goodspace.bllsoneshot.repository.task

import goodspace.bllsoneshot.entity.assignment.CommentType
import goodspace.bllsoneshot.entity.assignment.RegisterStatus
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.mentor.dto.response.FeedbackRequiredTaskResponse
import goodspace.bllsoneshot.mentor.dto.response.PendingUploadMenteeResponse
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<Task, Long> {

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :userId
        AND t.date = :date
        AND t.isResource = false
        """
    )
    fun findCurrentTasks(userId: Long, date: LocalDate): List<Task>

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :menteeId
        AND t.date BETWEEN :startDate AND :endDate
        """
    )
    fun findDateBetweenTasks(
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Task>

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id = :userId AND t.subject = :subject
        AND t.date < :date
        AND t.isResource = false
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
        AND t.isResource = false
        """
    )
    fun findByIdWithMenteeAndGeneralCommentAndProofShots(taskId: Long): Task?

    /*
    1. Task -> mentee 조인 => 멘티 정보 가져오기
    2. Task -> proofShots 조인 => 증빙샷이 있는 Task만 필터링(제출된 과제)
    3. 날짜 조건 => date가 과제의 date와 동일한 Task만 필터링
    4. AND NOT EXISTS 서브쿼리 => 해당 Task에 피드백 댓글이 없는 것만 필터링
    5. GROUP BY mentee.id, mentee.name => 멘티별로 그룹화
    6. SELECT new ... => 멘티 ID, 이름, 피드백이 필요한 Task 수를 DTO로 매핑
     */
    @Query(
        """
        SELECT new goodspace.bllsoneshot.mentor.dto.response.FeedbackRequiredTaskResponse(
            m.id,
            m.name,
            COUNT(DISTINCT t.id)
        )
        FROM Task t
        JOIN t.mentee m
        JOIN t.proofShots ps
        WHERE m.mentor.id = :mentorId
        AND t.date = :date
        AND t.isResource = false
        AND NOT EXISTS (
            SELECT c 
            FROM Comment c
            WHERE c.task = t
              AND c.type = :feedbackType
              AND c.registerStatus = :registeredStatus
        )
        GROUP BY m.id, m.name
        """
    )
    fun findFeedbackRequiredTasks(
        mentorId: Long,
        date: LocalDate,
        feedbackType: CommentType,
        registeredStatus: RegisterStatus
    ): List<FeedbackRequiredTaskResponse>

    @Query(
        """
        SELECT DISTINCT new goodspace.bllsoneshot.mentor.dto.response.PendingUploadMenteeResponse(
            m.id,
            m.name
        )
        FROM Task t
        JOIN t.mentee m
        LEFT JOIN t.proofShots ps
        WHERE m.mentor.id = :mentorId
        AND t.date = :date
        AND t.isResource = false
        AND ps.id IS NULL
        ORDER BY m.name
        """
    )
    fun findTaskUnfinishedMentees(
        mentorId: Long,
        date: LocalDate
    ): List<PendingUploadMenteeResponse>

    @Query(
        """
        SELECT COUNT(DISTINCT t.id)
        FROM Task t
        LEFT JOIN t.proofShots ps
        WHERE t.mentee.mentor.id = :mentorId
        AND t.date = :date
        AND t.isResource = false
        AND ps.id IS NULL
        """
    )
    fun countUnfinishedTasks(mentorId: Long, date: LocalDate): Long

    @Query(
        """
        SELECT DISTINCT t.mentee.id
        FROM Task t
        LEFT JOIN t.proofShots ps
        WHERE t.mentee.id IN :menteeIds
        AND t.date = :date
        AND t.isResource = false
        AND ps.id IS NULL
        """
    )
    fun findMenteeIdsWithUnsubmittedTasks(menteeIds: List<Long>, date: LocalDate): List<Long>

    @Query(
        """
        SELECT t FROM Task t
        WHERE t.mentee.id IN :menteeIds
        AND t.isResource = false
        AND t.date = (
            SELECT MAX(t2.date) FROM Task t2
            WHERE t2.mentee = t.mentee
            AND t2.isResource = false
        )
        ORDER BY t.mentee.id, t.id DESC
        """
    )
    fun findMostRecentTasksByMenteeIds(menteeIds: List<Long>): List<Task>

    @Query(
        """
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.worksheets
        WHERE t.mentee.id = :menteeId
        AND t.isResource = true
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun findResourcesByMenteeId(menteeId: Long): List<Task>
}
