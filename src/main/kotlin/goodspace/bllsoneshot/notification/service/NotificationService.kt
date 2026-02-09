package goodspace.bllsoneshot.notification.service

import goodspace.bllsoneshot.entity.assignment.Notification
import goodspace.bllsoneshot.entity.assignment.NotificationStatus
import goodspace.bllsoneshot.entity.assignment.NotificationType
import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.global.exception.ExceptionMessage.NOTIFICATION_NOT_FOUND
import goodspace.bllsoneshot.global.exception.ExceptionMessage.NOTIFICATION_ACCESS_DENIED
import goodspace.bllsoneshot.global.exception.ExceptionMessage.USER_NOT_FOUND
import goodspace.bllsoneshot.global.notification.PushNotificationSender
import goodspace.bllsoneshot.notification.dto.response.NotificationResponse
import goodspace.bllsoneshot.notification.dto.response.UnreadCountResponse
import goodspace.bllsoneshot.notification.mapper.NotificationMapper
import goodspace.bllsoneshot.repository.notification.NotificationRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val notificationMapper: NotificationMapper,
    private val pushNotificationSender: PushNotificationSender
) {
    private val logger = KotlinLogging.logger {}

    // ── 알림 조회 ─────────────────────────────────────

    /**
     * 알림 목록을 조회하고, NEW 상태인 알림을 UNREAD로 일괄 전이한다.
     * 프론트엔드에서 알림 목록 화면에 진입했다는 의미이므로
     * "알림이 왔다는 것을 확인했다"로 간주한다.
     */
    @Transactional
    fun getNotifications(userId: Long): List<NotificationResponse> {
        val notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId)

        notificationRepository.bulkUpdateStatus(
            receiverId = userId,
            currentStatus = NotificationStatus.NEW,
            newStatus = NotificationStatus.UNREAD
        )

        return notificationMapper.map(notifications)
    }

    /**
     * NEW 상태인 알림의 개수를 반환한다.
     * 프론트엔드 헤더의 알림 뱃지 등에 활용된다.
     */
    @Transactional(readOnly = true)
    fun getNewNotificationCount(userId: Long): UnreadCountResponse {
        val count = notificationRepository.countByReceiverIdAndStatus(
            receiverId = userId,
            status = NotificationStatus.NEW
        )
        return UnreadCountResponse(count = count)
    }

    /**
     * 특정 알림을 READ 상태로 전이한다.
     * 프론트엔드에서 알림을 클릭하여 상세 페이지로 이동할 때 호출된다.
     */
    @Transactional
    fun markAsRead(userId: Long, notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException(NOTIFICATION_NOT_FOUND.message) }

        check(notification.receiver.id == userId) { NOTIFICATION_ACCESS_DENIED.message }

        notification.markAsRead()
    }

    // ── FCM 토큰 등록 ─────────────────────────────────

    @Transactional
    fun registerFcmToken(userId: Long, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        user.fcmToken = token
    }

    // ── 알림 생성 (다른 서비스에서 호출) ──────────────────

    @Transactional
    fun notify(
        receiver: User,
        type: NotificationType,
        title: String,
        message: String,
        task: Task? = null,
        learningReport: LearningReport? = null
    ) {
        val notification = Notification(
            receiver = receiver,
            task = task,
            learningReport = learningReport,
            type = type,
            title = title,
            message = message
        )
        notificationRepository.save(notification)

        sendPush(receiver, title, message)
    }

    // ── 푸시 전송 (실패해도 알림 저장에는 영향 없음) ──────

    private fun sendPush(receiver: User, title: String, body: String) {
        val token = receiver.fcmToken ?: return

        try {
            pushNotificationSender.send(targetToken = token, title = title, body = body)
        } catch (e: Exception) {
            logger.warn { "푸시 알림 전송 실패 (userId=${receiver.id}): ${e.message}" }
        }
    }
}
