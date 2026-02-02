package goodspace.bllsoneshot.global.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.google.firebase.messaging.WebpushConfig
import com.google.firebase.messaging.WebpushFcmOptions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PushNotificationSender(
    private val firebaseMessaging: FirebaseMessaging
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(
        targetToken: String,
        title: String,
        body: String,
        targetUrl: String? = null
    ) {
        val notification = Notification.builder().apply {
            setTitle(title)
            setBody(body)
        }.build()

        val message = Message.builder().apply {
            setToken(targetToken)
            setNotification(notification)

            targetUrl?.let { url ->
                setWebpushConfig(
                    WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.builder().setLink(url).build())
                        .build()
                )
            }
        }.build()

        runCatching {
            firebaseMessaging.send(message)
        }.onFailure { e ->
            logger.error("FCM 메시지 전송 실패: ${e.message}")
            throw RuntimeException("알림 전송 중 오류가 발생했습니다.", e)
        }
    }
}
