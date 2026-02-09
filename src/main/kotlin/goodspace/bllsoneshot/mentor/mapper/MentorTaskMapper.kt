package goodspace.bllsoneshot.mentor.mapper

import goodspace.bllsoneshot.entity.assignment.ProofShot
import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskDetailResponse
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskEditResponse
import goodspace.bllsoneshot.task.dto.response.feedback.ProofShotResponse
import goodspace.bllsoneshot.task.mapper.ColumnLinkMapper
import goodspace.bllsoneshot.task.mapper.FeedbackMapper
import goodspace.bllsoneshot.task.mapper.QuestionMapper
import goodspace.bllsoneshot.task.mapper.WorksheetMapper
import org.springframework.stereotype.Component

@Component
class MentorTaskMapper(
    private val questionMapper: QuestionMapper,
    private val feedbackMapper: FeedbackMapper,
    private val worksheetMapper: WorksheetMapper,
    private val columnLinkMapper: ColumnLinkMapper
) {

    fun mapToEdit(task: Task): MentorTaskEditResponse {
        return MentorTaskEditResponse(
            subject = task.subject,
            date = task.date,
            taskName = task.name,
            goalMinutes = task.goalMinutes,
            completed = task.completed,
            worksheets = task.worksheets.map { worksheetMapper.map(it) },
            columnLinks = task.columnLinks.map { columnLinkMapper.map(it) }
        )
    }

    fun mapToDetail(task: Task): MentorTaskDetailResponse {
        return MentorTaskDetailResponse(
            taskId = task.id!!,
            taskName = task.name,
            subject = task.subject,
            menteeName = task.mentee.name,
            generalComment = task.generalComment?.content,
            hasProofShot = task.hasProofShot(),
            hasFeedback = task.hasFeedback(),
            proofShots = task.proofShots.map { mapProofShot(it) }
        )
    }

    /**
     * 멘토 화면용 ProofShot 매핑.
     * 멘티 화면과 달리 임시저장(TEMPORARY) 피드백도 포함합니다.
     */
    private fun mapProofShot(proofShot: ProofShot): ProofShotResponse {
        val questions = proofShot.questComments.sortedBy { it.annotation.number }
        val feedbacks = proofShot.allFeedbackComments.sortedBy { it.annotation.number }

        return ProofShotResponse(
            proofShotId = proofShot.id!!,
            imageFileId = proofShot.file.id!!,
            questions = questions.map { questionMapper.map(it) },
            feedbacks = feedbacks.map { feedbackMapper.map(it) }
        )
    }
}
