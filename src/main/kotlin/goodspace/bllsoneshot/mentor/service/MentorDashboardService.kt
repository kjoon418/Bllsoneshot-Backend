package goodspace.bllsoneshot.mentor.service

import goodspace.bllsoneshot.entity.assignment.CommentType
import goodspace.bllsoneshot.entity.assignment.RegisterStatus
import goodspace.bllsoneshot.global.exception.ExceptionMessage.USER_NOT_FOUND
import goodspace.bllsoneshot.mentor.dto.response.FeedbackRequiredTaskSummaryResponse
import goodspace.bllsoneshot.mentor.dto.response.MenteeManagementDetailResponse
import goodspace.bllsoneshot.mentor.dto.response.MenteeManagementSummaryResponse
import goodspace.bllsoneshot.mentor.dto.response.TaskUnfinishedSummaryResponse
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MentorDashboardService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getFeedbackRequiredTasks(
        mentorId: Long,
        date: LocalDate
    ): FeedbackRequiredTaskSummaryResponse {
        userRepository.findById(mentorId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        val tasks = taskRepository.findFeedbackRequiredTasks(
            mentorId = mentorId,
            date = date,
            feedbackType = CommentType.FEEDBACK,
            confirmedStatus = RegisterStatus.CONFIRMED
        )

        return FeedbackRequiredTaskSummaryResponse(
            taskCount = tasks.sumOf { it.submittedTaskCount },
            menteeNames = tasks.map { it.menteeName }.distinct()
        )
    }

    @Transactional(readOnly = true)
    fun getTaskIncompletedMentees(
        mentorId: Long,
        date: LocalDate
    ): TaskUnfinishedSummaryResponse {
        userRepository.findById(mentorId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        val taskCount = taskRepository.countUnfinishedTasks(mentorId, date)
        val mentees = taskRepository.findTaskIncompletedMentees(
            mentorId = mentorId,
            date = date
        )

        return TaskUnfinishedSummaryResponse(
            taskCount = taskCount,
            menteeCount = mentees.size,
            menteeNames = mentees.map { it.menteeName },
        )
    }

    @Transactional(readOnly = true)
    fun getMenteeManagementList(
        mentorId: Long,
        date: LocalDate
    ): MenteeManagementSummaryResponse {
        userRepository.findById(mentorId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        val mentees = userRepository.findMenteesWithSubjectsByMentorId(mentorId)
        if (mentees.isEmpty()) {
            return MenteeManagementSummaryResponse(
                totalMenteeCount = 0,
                submittedMenteeCount = 0,
                notSubmittedMenteeCount = 0,
                mentees = emptyList()
            )
        }

        val menteeIds = mentees.map { it.id!! }

        val incompletedMenteeIds = taskRepository
            .findMenteeIdsWithIncompletedTasks(menteeIds, date)
            .toSet()

        val recentTaskByMenteeId = taskRepository
            .findMostRecentTaskByMenteeIds(menteeIds)
            .groupBy { it.mentee.id!! }
            .mapValues { (_, tasks) -> tasks.first() } // completedAt DESC이므로 first가 가장 최근

        val menteeDetails = mentees.map { mentee ->
            val recentTask = recentTaskByMenteeId[mentee.id]
            MenteeManagementDetailResponse(
                menteeId = mentee.id!!,
                menteeName = mentee.name,
                grade = mentee.grade,
                subjects = mentee.subjects.map { it.subject },
                recentTaskDate = recentTask?.date,
                recentTaskName = recentTask?.name,
                submitted = mentee.id!! !in incompletedMenteeIds
            )
        }

        val submittedCount = menteeDetails.count { it.submitted }

        return MenteeManagementSummaryResponse(
            totalMenteeCount = mentees.size,
            submittedMenteeCount = submittedCount,
            notSubmittedMenteeCount = mentees.size - submittedCount,
            mentees = menteeDetails
        )
    }
}
