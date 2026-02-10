package goodspace.bllsoneshot.report.service

import goodspace.bllsoneshot.entity.assignment.GeneralComment
import goodspace.bllsoneshot.entity.assignment.NotificationType
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.notification.service.NotificationService
import goodspace.bllsoneshot.repository.user.LearningReportRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.report.dto.request.ReportCreateRequest
import goodspace.bllsoneshot.report.dto.response.ReportResponse
import goodspace.bllsoneshot.report.dto.response.ReportTaskResponse
import goodspace.bllsoneshot.report.mapper.ReportMapper
import goodspace.bllsoneshot.report.mapper.ReportTaskMapper
import goodspace.bllsoneshot.repository.task.TaskRepository
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val userRepository: UserRepository,
    private val learningReportRepository: LearningReportRepository,
    private val taskRepository: TaskRepository,
    private val reportMapper: ReportMapper,
    private val notificationService: NotificationService,
    private val reportTaskMapper: ReportTaskMapper
) {

    @Transactional
    fun createLearningReport(mentorId: Long, menteeId: Long, request: ReportCreateRequest): ReportResponse {
        val mentee = findUserBy(menteeId)

        validateAssignedMentee(mentorId, mentee)
        validateReportDuplicate(menteeId, request.subject, request.startDate, request.endDate)

        val report = learningReportRepository.save(
            LearningReport(
                mentee = mentee,
                generalComment = GeneralComment(content = request.generalComment.trim()),
                subject = request.subject,
                startDate = request.startDate,
                endDate = request.endDate,
                goodPointContents = request.goodPoints,
                badPointContents = request.badPoints
            )
        )

        // 멘티에게 학습 리포트 알림 전송
        val mentorName = mentee.mentor?.name ?: "멘토"
        val weekLabel = formatWeekLabel(request.startDate)
        notificationService.notify(
            receiver = mentee,
            type = NotificationType.LEARNING_REPORT,
            title = "학습 리포트 도착",
            message = "${mentorName} 멘토의 ${weekLabel} 학습리포트가 도착했어요!",
            learningReport = report
        )

        return reportMapper.map(report)
    }

    private fun formatWeekLabel(startDate: LocalDate): String {
        val month = startDate.monthValue
        val weekOfMonth = (startDate.dayOfMonth - 1) / 7 + 1
        return "${month}월 ${weekOfMonth}주차"
    }

    @Transactional(readOnly = true)
    fun getReport(
        mentorId: Long,
        menteeId: Long,
        subject: Subject,
        startDate: LocalDate,
        endDate: LocalDate
    ): ReportResponse {
        val mentee = findUserBy(menteeId)

        validateAssignedMentee(mentorId, mentee)

        val report = learningReportRepository.findByMenteeIdAndSubjectAndStartDateAndEndDate(
            menteeId = menteeId,
            subject = subject,
            startDate = startDate,
            endDate = endDate
        ) ?: throw IllegalArgumentException(ExceptionMessage.REPORT_NOT_FOUND.message)

        return reportMapper.map(report)
    }

    @Transactional(readOnly = true)
    fun getReceivedReport(
        menteeId: Long,
        subject: Subject,
        date: LocalDate
    ): ReportTaskResponse {
        val report = learningReportRepository.findByMenteeIdAndSubjectContainingDate(
            menteeId = menteeId,
            subject = subject,
            date = date
        ) ?: throw IllegalArgumentException(ExceptionMessage.REPORT_NOT_FOUND.message)

        val tasks = taskRepository.findDateBetweenTasks(
            menteeId = menteeId,
            subject = report.subject,
            startDate = report.startDate,
            endDate = report.endDate
        )

        return reportTaskMapper.map(report, tasks)
    }

    @Transactional(readOnly = true)
    fun getReceivedReportById(
        menteeId: Long,
        reportId: Long
    ): ReportTaskResponse {
        val report = learningReportRepository.findById(reportId)
            .orElseThrow { IllegalArgumentException(ExceptionMessage.REPORT_NOT_FOUND.message) }

        validateReportOwnership(report, menteeId)

        val tasks = taskRepository.findDateBetweenTasks(
            menteeId = menteeId,
            subject = report.subject,
            startDate = report.startDate,
            endDate = report.endDate
        )

        return reportTaskMapper.map(report, tasks)
    }

    private fun findUserBy(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(ExceptionMessage.USER_NOT_FOUND.message) }
    }

    private fun validateAssignedMentee(
        mentorId: Long,
        mentee: User
    ) {
        check(mentee.mentor?.id == mentorId) { ExceptionMessage.MENTEE_ACCESS_DENIED.message }
    }

    private fun validateReportOwnership(report: LearningReport, menteeId: Long) {
        check(report.mentee.id == menteeId) { ExceptionMessage.MENTEE_ACCESS_DENIED.message }
    }

    private fun validateReportDuplicate(
        menteeId: Long,
        subject: Subject,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val existsDuplicate = learningReportRepository.existsByMenteeIdAndSubjectAndStartDateAndEndDate(
            menteeId = menteeId,
            subject = subject,
            startDate = startDate,
            endDate = endDate
        )
        check(!existsDuplicate) { ExceptionMessage.REPORT_DUPLICATE.message }
    }
}
