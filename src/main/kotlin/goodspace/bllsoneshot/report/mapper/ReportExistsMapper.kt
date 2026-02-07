package goodspace.bllsoneshot.report.mapper

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.repository.user.LearningReportRepository
import goodspace.bllsoneshot.report.dto.response.ReportExistsResponse
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class ReportExistsMapper(
    private val learningReportRepository: LearningReportRepository
) {

    fun map(
        menteeId: Long,
        subject: Subject,
        startDate: LocalDate,
        endDate: LocalDate
    ): ReportExistsResponse {
        val exists = learningReportRepository.existsByMenteeIdAndSubjectAndStartDateAndEndDate(
            menteeId = menteeId,
            subject = subject,
            startDate = startDate,
            endDate = endDate
        )

        return ReportExistsResponse(subject = subject, exists = exists)
    }
}
