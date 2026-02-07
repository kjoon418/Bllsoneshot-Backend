package goodspace.bllsoneshot.report.mapper

import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.report.dto.response.ReportTaskResponse
import org.springframework.stereotype.Component

@Component
class ReportTaskMapper(
    private val reportMapper: ReportMapper
) {

    fun map(report: LearningReport, tasks: List<Task>): ReportTaskResponse {
        val taskAmount = tasks.size
        val completedTaskAmount = tasks.count { it.completed }
        val goalMinutesTotal = tasks.sumOf { it.goalMinutes }
        val actualMinutesTotal = tasks.sumOf { it.actualMinutes ?: 0 }

        return ReportTaskResponse(
            taskAmount = taskAmount,
            completedTaskAmount = completedTaskAmount,
            goalMinutesTotal = goalMinutesTotal,
            actualMinutesTotal = actualMinutesTotal,
            report = reportMapper.map(report)
        )
    }
}
