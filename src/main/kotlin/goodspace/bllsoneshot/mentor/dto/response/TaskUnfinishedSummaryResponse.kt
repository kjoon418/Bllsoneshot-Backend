package goodspace.bllsoneshot.mentor.dto.response

data class TaskUnfinishedSummaryResponse(
    val taskCount: Long,
    val menteeCount: Int,
    val menteeNames: List<String>,
)
