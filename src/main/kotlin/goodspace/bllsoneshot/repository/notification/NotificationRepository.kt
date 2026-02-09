package goodspace.bllsoneshot.repository.notification

import goodspace.bllsoneshot.entity.assignment.Notification
import goodspace.bllsoneshot.entity.assignment.NotificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<Notification, Long> {

    fun findByReceiverIdOrderByCreatedAtDesc(receiverId: Long): List<Notification>

    fun countByReceiverIdAndStatus(receiverId: Long, status: NotificationStatus): Long

    // 알림 목록 조회 시 NEW 상태인 알림을 일괄 UNREAD로 전이
    @Modifying
    @Query(
        """
        UPDATE Notification n
        SET n.status = :newStatus
        WHERE n.receiver.id = :receiverId
        AND n.status = :currentStatus
        """
    )
    fun bulkUpdateStatus(receiverId: Long, currentStatus: NotificationStatus, newStatus: NotificationStatus)
}
