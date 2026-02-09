package goodspace.bllsoneshot.mentor.service

import goodspace.bllsoneshot.entity.assignment.*
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.*
import goodspace.bllsoneshot.mentor.dto.request.MentorFeedbackRequest
import goodspace.bllsoneshot.mentor.dto.request.MentorTaskUpdateRequest
import goodspace.bllsoneshot.mentor.dto.request.QuestionAnswerRequest
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskDetailResponse
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskEditResponse
import goodspace.bllsoneshot.mentor.mapper.MentorTaskMapper
import goodspace.bllsoneshot.notification.service.NotificationService
import goodspace.bllsoneshot.repository.file.FileRepository
import goodspace.bllsoneshot.repository.task.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MentorTaskService(
    private val taskRepository: TaskRepository,
    private val fileRepository: FileRepository,
    private val mentorTaskMapper: MentorTaskMapper,
    private val notificationService: NotificationService
) {

    @Transactional(readOnly = true)
    fun getTaskForFeedback(mentorId: Long, taskId: Long): MentorTaskDetailResponse {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)

        return mentorTaskMapper.mapToDetail(task)
    }

    @Transactional(readOnly = true)
    fun getTemporaryFeedback(mentorId: Long, taskId: Long): MentorTaskDetailResponse {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)

        return mentorTaskMapper.mapToTemporaryDetail(task)
    }

    @Transactional
    fun saveTemporary(mentorId: Long, taskId: Long, request: MentorFeedbackRequest) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)
        validateGeneralCommentLength(request.generalComment)

        // 임시 저장 데이터만 제거 후 덮어쓰기
        task.clearTemporaryFeedbackComments()
        updateTemporaryGeneralComment(task, request.generalComment)
        createFeedbackComments(task, request, RegisterStatus.TEMPORARY)
        updateTemporaryAnswers(task, request.questionAnswers)

        taskRepository.save(task)
    }

    @Transactional
    fun saveFeedback(mentorId: Long, taskId: Long, request: MentorFeedbackRequest) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)
        validateFinalFeedbackRequest(request)

        // 모든 데이터 제거 후 덮어쓰기
        task.clearFeedbackComments()
        updateGeneralComment(task, request.generalComment)
        createFeedbackComments(task, request, RegisterStatus.CONFIRMED)

        // 최종 저장 시 모든 임시저장 답변 제거 후 새 답변 저장
        task.clearTemporaryAnswers()
        updateAnswers(task, request.questionAnswers)

        taskRepository.save(task)

        // 멘티에게 피드백 알림 전송
        val mentorName = task.mentee.mentor?.name ?: "멘토"
        notificationService.notify(
            receiver = task.mentee,
            type = NotificationType.FEEDBACK,
            title = "피드백 도착",
            message = "할 일 '${task.name}'에 멘토 ${mentorName}의 피드백이 달렸어요!",
            task = task
        )
    }

    @Transactional
    fun deleteFeedback(mentorId: Long, taskId: Long) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)

        task.clearFeedbackComments()
        task.generalComment = null

        taskRepository.save(task)
    }

    @Transactional
    fun updateTask(mentorId: Long, taskId: Long, request: MentorTaskUpdateRequest): MentorTaskEditResponse {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)

        task.subject = request.subject
        task.name = request.taskName
        task.goalMinutes = request.goalMinutes
        replaceWorksheets(task, request)
        replaceColumnLinks(task, request)

        return mentorTaskMapper.mapToEdit(task)
    }

    @Transactional
    fun deleteTask(mentorId: Long, taskId: Long) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)
        validateCreatedByMentor(task)

        taskRepository.delete(task)
    }

    // ── 조회 ────────────────────────────────────────────

    private fun findTaskWithDetails(taskId: Long): Task {
        return taskRepository.findByIdWithMenteeAndGeneralCommentAndProofShots(taskId)
            ?: throw IllegalArgumentException(TASK_NOT_FOUND.message)
    }

    // ── 검증 ────────────────────────────────────────────

    private fun validateMentorAccess(mentorId: Long, task: Task) {
        check(task.mentee.mentor?.id == mentorId) { MENTEE_ACCESS_DENIED.message }
    }

    private fun validateCreatedByMentor(task: Task) {
        check(task.createdBy == UserRole.ROLE_MENTOR) { TASK_NOT_CREATED_BY_MENTOR.message }
    }

    // ── 학습 자료 · 칼럼 링크 교체 ──────────────────────

    private fun replaceWorksheets(task: Task, request: MentorTaskUpdateRequest) {
        task.worksheets.clear()
        task.worksheets.addAll(
            request.worksheets
                .mapNotNull { it.fileId }
                .mapNotNull { fileId ->
                    fileRepository.findById(fileId).orElse(null)
                        ?.let { file -> Worksheet(task, file) }
                }
        )
    }

    private fun replaceColumnLinks(task: Task, request: MentorTaskUpdateRequest) {
        task.columnLinks.clear()
        task.columnLinks.addAll(
            request.columnLinks
                .mapNotNull { it.link?.takeIf { link -> link.isNotBlank() } }
                .map { link -> ColumnLink(task, link) }
        )
    }

    private fun validateGeneralCommentLength(generalComment: String?) {
        if (generalComment != null) {
            require(generalComment.length <= MAX_GENERAL_COMMENT_LENGTH) {
                GENERAL_COMMENT_TOO_LONG.message
            }
        }
    }

    private fun validateFinalFeedbackRequest(request: MentorFeedbackRequest) {
        validateGeneralCommentLength(request.generalComment)
        require(!request.generalComment.isNullOrBlank()) {
            GENERAL_COMMENT_REQUIRED.message
        }
        request.proofShotFeedbacks
            .flatMap { it.feedbacks }
            .forEach { feedback ->
                require(feedback.content.isNotBlank()) {
                    FEEDBACK_CONTENT_BLANK.message
                }
            }
    }

    // ── 피드백 교체 (공통 로직) ──────────────────────────

    // TODO: 로직 이해하기
    //  피드백 저장 흐름: ①기존 피드백 전체 삭제 → ②총평 생성/갱신 → ③상세 피드백 재생성
    //  매 저장마다 전체 교체(replace-all) 방식을 사용한다.
    //  이유: 멘티의 submitTask와 동일한 패턴이며, 부분 수정보다 상태 관리가 단순하다.
    private fun replaceFeedback(task: Task, request: MentorFeedbackRequest, status: RegisterStatus) {
        task.clearFeedbackComments()
        updateGeneralComment(task, request.generalComment)
        createFeedbackComments(task, request, status)

        taskRepository.save(task)
    }

    // ── 총평 저장 ───────────────────────────────────────

    // TODO: 로직 이해하기
    //  총평이 비어 있으면 → null로 설정 (orphanRemoval로 DB에서 삭제됨)
    //  이미 총평이 있으면  → 기존 엔티티의 content만 갱신 (불필요한 DELETE+INSERT 방지)
    //  총평이 없으면      → 새 GeneralComment 생성 (cascade PERSIST로 자동 저장됨)
    private fun updateGeneralComment(task: Task, generalComment: String?) {
        if (generalComment.isNullOrBlank()) {
            task.generalComment = null
            return
        }

        val existing = task.generalComment
        if (existing != null) {
            existing.content = generalComment
            existing.temporaryContent = null
        } else {
            task.generalComment = GeneralComment(content = generalComment)
        }
    }

    private fun updateTemporaryGeneralComment(task: Task, temporaryContent: String?) {
        // 빈 값이면 null로 저장
        val contentToSave = temporaryContent?.ifBlank { null }

        val existing = task.generalComment
        if (existing != null) {
            existing.temporaryContent = contentToSave
        } else {
            // GeneralComment가 없고 빈 값이면 생성하지 않음
            if (contentToSave != null) {
                task.generalComment = GeneralComment(temporaryContent = contentToSave)
            }
        }
    }

    private fun createFeedbackComments(task: Task, request: MentorFeedbackRequest, status: RegisterStatus) {
        val proofShotMap = task.proofShots.associateBy { it.id!! }

        for (proofShotFeedback in request.proofShotFeedbacks) {
            val proofShot = proofShotMap[proofShotFeedback.proofShotId]
                ?: throw IllegalArgumentException(PROOF_SHOT_NOT_FOUND.message)
            for ((index, feedback) in proofShotFeedback.feedbacks.withIndex()) {
                val annotation = CommentAnnotation(
                    number = index + 1,
                    percentX = feedback.percentX,
                    percentY = feedback.percentY
                )
                val comment = Comment(
                    proofShot = proofShot,
                    annotation = annotation,
                    content = feedback.content,
                    starred = feedback.starred,
                    type = CommentType.FEEDBACK,
                    registerStatus = status
                )

                proofShot.comments.add(comment)
            }
        }
    }

    private fun updateTemporaryAnswers(task: Task, questionAnswers: List<QuestionAnswerRequest>) {
        val questionMap = task.questions.associateBy { it.id!! }

        for (questionAnswer in questionAnswers) {
            val question = questionMap[questionAnswer.questionId]
                ?: throw IllegalArgumentException("질문을 찾을 수 없습니다: ${questionAnswer.questionId}")

            // 빈 값이면 null로 저장
            val contentToSave = questionAnswer.content.ifBlank { null }

            val existingAnswer = question.answer
            if (existingAnswer != null) {
                existingAnswer.temporaryContent = contentToSave
            } else {
                // Answer가 없고 빈 값이면 생성하지 않음
                if (contentToSave != null) {
                    question.answer = Answer(
                        content = null,
                        temporaryContent = contentToSave
                    )
                }
            }
        }
    }

    private fun updateAnswers(task: Task, questionAnswers: List<QuestionAnswerRequest>) {
        val questionMap = task.questions.associateBy { it.id!! }

        for (questionAnswer in questionAnswers) {
            val question = questionMap[questionAnswer.questionId]
                ?: throw IllegalArgumentException("질문을 찾을 수 없습니다: ${questionAnswer.questionId}")

            require(questionAnswer.content.isNotBlank()) {
                "답변 내용이 비어있습니다: questionId=${questionAnswer.questionId}"
            }

            val existingAnswer = question.answer
            if (existingAnswer != null) {
                existingAnswer.content = questionAnswer.content
                existingAnswer.temporaryContent = null
            } else {
                question.answer = Answer(
                    content = questionAnswer.content,
                    temporaryContent = null
                )
            }
        }
    }

    companion object {
        private const val MAX_GENERAL_COMMENT_LENGTH = 200
    }
}
