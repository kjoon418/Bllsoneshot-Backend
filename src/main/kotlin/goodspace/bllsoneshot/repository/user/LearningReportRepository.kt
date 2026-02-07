package goodspace.bllsoneshot.repository.user

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.LearningReport
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LearningReportRepository : JpaRepository<LearningReport, Long> {

    @Query(
        """
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM LearningReport r
        WHERE r.mentee.id = :menteeId
        AND r.subject = :subject
        AND r.startDate = :startDate
        AND r.endDate = :endDate
        """
    )
    fun existsByMenteeIdAndSubjectAndStartDateAndEndDate(
        menteeId: Long,
        subject: Subject,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean

    fun findByMenteeIdAndSubjectAndStartDateAndEndDate(
        menteeId: Long,
        subject: Subject,
        startDate: LocalDate,
        endDate: LocalDate
    ): LearningReport?

    @Query(
        """
        SELECT r FROM LearningReport r
        WHERE r.mentee.id = :menteeId
        AND r.subject = :subject
        AND :date BETWEEN r.startDate AND r.endDate
        """
    )
    fun findByMenteeIdAndSubjectContainingDate(
        menteeId: Long,
        subject: Subject,
        date: LocalDate
    ): LearningReport?

    @Query(
        """
        SELECT COUNT(r) FROM LearningReport r
        WHERE r.mentee.id = :menteeId
        AND :date BETWEEN r.startDate AND r.endDate
        """
    )
    fun countByMenteeIdAndDate(menteeId: Long, date: LocalDate): Long

    @Query(
        """
        SELECT DISTINCT r.subject FROM LearningReport r
        WHERE r.mentee.id = :menteeId
        AND :date BETWEEN r.startDate AND r.endDate
        """
    )
    fun findSubjectsByMenteeIdAndDate(menteeId: Long, date: LocalDate): List<Subject>
}
