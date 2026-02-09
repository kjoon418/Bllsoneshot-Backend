package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn
import java.time.LocalDateTime

@Entity
class Notification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val receiver: User,

    @ManyToOne(fetch = FetchType.LAZY)
    val task: Task? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val learningReport: LearningReport? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val message: String,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {

    /*
    코틀린은 원래 상속X가 디폴트
    1. final로 상속 안할거라고 명시 -> 프록시 못씀
    2. open으로 상속할거라고 명시 + setter를 protected -> 프록시 가능
    */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.NEW
        protected set

    fun markAsChecked() {
        if (status == NotificationStatus.NEW) {
            status = NotificationStatus.UNREAD
        }
    }

    fun markAsRead() {
        status = NotificationStatus.READ
    }
}
