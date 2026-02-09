package goodspace.bllsoneshot.notification.scheduler

import goodspace.bllsoneshot.entity.assignment.NotificationType
import goodspace.bllsoneshot.notification.service.NotificationService
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 매일 19:00(KST)에 실행되는 리마인더 스케줄러.
 * - 멘티: 오늘 할 일 중 미완료(ProofShot 미제출) 건이 있으면 알림
 */
@Component
class ReminderScheduler(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    @Transactional
    fun sendDailyReminders() {
        val today = LocalDate.now()

        logger.info { "리마인더 스케줄러 실행: $today" }

        sendMenteeReminders(today)
    }

    // ── 멘티 리마인더 ─────────────────────────────────

    /**
     * 오늘 할 일이 있지만 ProofShot을 제출하지 않은 멘티에게 리마인더를 보낸다.
     */
    private fun sendMenteeReminders(date: LocalDate) {
        val unfinishedCounts = taskRepository.countUnfinishedTasksByMentee(date)

        for (unfinished in unfinishedCounts) {
            val mentee = userRepository.findById(unfinished.menteeId).orElse(null) ?: continue

            notificationService.notify(
                receiver = mentee,
                type = NotificationType.REMINDER,
                title = "오늘의 할 일 알림",
                message = "오늘의 할 일 ${unfinished.count}개가 남아있어요. 지금 바로 공부를 시작해보세요!💪"
            )
        }

        logger.info { "멘티 리마인더 ${unfinishedCounts.size}건 전송 완료" }
    }
}
