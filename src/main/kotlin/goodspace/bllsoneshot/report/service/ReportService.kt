package goodspace.bllsoneshot.report.service

import goodspace.bllsoneshot.entity.assignment.GeneralComment
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.user.LearningReport
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.repository.user.LearningReportRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.report.dto.request.ReportCreateRequest
import goodspace.bllsoneshot.report.dto.response.ReportAmountResponse
import goodspace.bllsoneshot.report.dto.response.ReportExistResponse
import goodspace.bllsoneshot.report.dto.response.ReportExistsResponse
import goodspace.bllsoneshot.report.dto.response.ReportResponse
import goodspace.bllsoneshot.report.dto.response.ReportTaskResponse
import goodspace.bllsoneshot.report.mapper.ReportExistsMapper
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
    private val reportExistsMapper: ReportExistsMapper,
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

        return reportMapper.map(report)
    }

    @Transactional(readOnly = true)
    fun getReportExists(
        mentorId: Long,
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ReportExistsResponse> {
        val mentee = findUserBy(menteeId)
        validateAssignedMentee(mentorId, mentee)

        return Subject.entriesExcludeResource()
            .map {
                reportExistsMapper.map(
                    menteeId = menteeId,
                    subject = it,
                    startDate = startDate,
                    endDate = endDate
                )
            }
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

        // TODO: 자료가 아닌 할 일만 조회하도록 수정.
        val tasks = taskRepository.findDateBetweenTasks(menteeId, report.startDate, report.endDate)
            .filter { it.subject == report.subject }

        return reportTaskMapper.map(report, tasks)
    }

    @Transactional(readOnly = true)
    fun getReceivedReportAmount(menteeId: Long, date: LocalDate): ReportAmountResponse {
        val count = learningReportRepository.countByMenteeIdAndDate(menteeId, date)

        return ReportAmountResponse(amount = count.toInt())
    }

    @Transactional(readOnly = true)
    fun getReportExistSubjects(menteeId: Long, date: LocalDate): ReportExistResponse {
        val subjects = learningReportRepository.findSubjectsByMenteeIdAndDate(menteeId, date)

        return ReportExistResponse(subjects = subjects)
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
