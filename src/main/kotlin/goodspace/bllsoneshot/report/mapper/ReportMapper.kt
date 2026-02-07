package goodspace.bllsoneshot.report.mapper

import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.report.dto.response.ReportResponse
import org.springframework.stereotype.Component

@Component
class ReportMapper {

    fun map(report: LearningReport): ReportResponse {
        return ReportResponse(
            reportId = report.id!!,
            subject = report.subject,
            startDate = report.startDate,
            endDate = report.endDate,
            generalComment = report.generalComment.content,
            goodPoints = report.goodPoints.map { it.content },
            badPoints = report.badPoints.map { it.content }
        )
    }
}
