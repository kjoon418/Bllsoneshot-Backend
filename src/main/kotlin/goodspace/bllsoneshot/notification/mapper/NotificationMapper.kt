package goodspace.bllsoneshot.notification.mapper

import goodspace.bllsoneshot.entity.assignment.Notification
import goodspace.bllsoneshot.notification.dto.response.NotificationResponse
import org.springframework.stereotype.Component

@Component
class NotificationMapper {

    fun map(notification: Notification): NotificationResponse {
        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type,
            title = notification.title,
            message = notification.message,
            status = notification.status,
            taskId = notification.task?.id,
            taskSubject = notification.task?.subject,
            taskName = notification.task?.name,
            learningReportId = notification.learningReport?.id,
            learningReportStartDate = notification.learningReport?.startDate,
            learningReportEndDate = notification.learningReport?.endDate,
            createdAt = notification.createdAt
        )
    }

    fun map(notifications: List<Notification>): List<NotificationResponse> =
        notifications.map { map(it) }
}
