package goodspace.bllsoneshot.global.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.google.firebase.messaging.WebpushConfig
import com.google.firebase.messaging.WebpushFcmOptions
import org.springframework.stereotype.Component

@Component
class PushNotificationSender(
    private val firebaseMessaging: FirebaseMessaging
) {

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

        firebaseMessaging.send(message)
    }
}
