package goodspace.bllsoneshot.report.dto.response

data class ReportTaskResponse(
    val taskAmount: Int,
    val completedTaskAmount: Int,

    val goalMinutesTotal: Int,
    val actualMinutesTotal: Int,

    val report: ReportResponse
)
