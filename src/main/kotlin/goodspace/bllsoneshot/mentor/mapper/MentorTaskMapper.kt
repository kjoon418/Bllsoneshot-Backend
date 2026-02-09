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
            proofShots = task.proofShots.map { mapProofShot(it) }
        )
    }

    /**
     * 임시저장 피드백 조회용 매핑.
     * TEMPORARY 상태의 총평(temporaryContent) 및 피드백만 포함합니다.
     */
    fun mapToTemporaryDetail(task: Task): MentorTaskDetailResponse {
        return MentorTaskDetailResponse(
            taskId = task.id!!,
            taskName = task.name,
            subject = task.subject,
            menteeName = task.mentee.name,
            generalComment = task.generalComment?.temporaryContent,
            proofShots = task.proofShots.map { mapTemporaryProofShot(it) }
        )
    }

    /**
     * 멘토 화면용 ProofShot 매핑.
     * 최종 저장된(CONFIRMED) 피드백만 포함합니다.
     */
    private fun mapProofShot(proofShot: ProofShot): ProofShotResponse {
        val questions = proofShot.questComments.sortedBy { it.annotation.number }
        val feedbacks = proofShot.confirmedFeedbackComments.sortedBy { it.annotation.number }

        return ProofShotResponse(
            proofShotId = proofShot.id!!,
            imageFileId = proofShot.file.id!!,
            questions = questions.map { questionMapper.mapConfirmed(it) },
            feedbacks = feedbacks.map { feedbackMapper.map(it) }
        )
    }

    /**
     * 임시저장 피드백용 ProofShot 매핑.
     * TEMPORARY 상태의 피드백만 포함합니다.
     * 질문 답변은 임시저장된 답변(temporaryContent)을 반환합니다.
     */
    private fun mapTemporaryProofShot(proofShot: ProofShot): ProofShotResponse {
        val questions = proofShot.questComments.sortedBy { it.annotation.number }
        val feedbacks = proofShot.temporaryFeedbackComments.sortedBy { it.annotation.number }

        return ProofShotResponse(
            proofShotId = proofShot.id!!,
            imageFileId = proofShot.file.id!!,
            questions = questions.map { questionMapper.mapTemporary(it) },
            feedbacks = feedbacks.map { feedbackMapper.map(it) }
        )
    }
}
