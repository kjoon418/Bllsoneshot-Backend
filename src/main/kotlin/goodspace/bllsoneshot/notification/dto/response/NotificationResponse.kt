package goodspace.bllsoneshot.notification.dto.response

import goodspace.bllsoneshot.entity.assignment.NotificationStatus
import goodspace.bllsoneshot.entity.assignment.NotificationType
import java.time.LocalDate
import java.time.LocalDateTime

data class NotificationResponse(
    val notificationId: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val status: NotificationStatus,
    val taskId: Long?,
    val learningReportId: Long?,
    val learningReportStartDate: LocalDate?,
    val learningReportEndDate: LocalDate?,
    val createdAt: LocalDateTime
)
