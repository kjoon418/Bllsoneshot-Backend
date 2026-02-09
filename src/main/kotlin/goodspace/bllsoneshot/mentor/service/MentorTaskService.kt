package goodspace.bllsoneshot.mentor.service

import goodspace.bllsoneshot.entity.assignment.*
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.*
import goodspace.bllsoneshot.mentor.dto.request.MentorFeedbackRequest
import goodspace.bllsoneshot.mentor.dto.request.MentorTaskUpdateRequest
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskDetailResponse
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskEditResponse
import goodspace.bllsoneshot.mentor.mapper.MentorTaskMapper
import goodspace.bllsoneshot.repository.file.FileRepository
import goodspace.bllsoneshot.repository.task.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MentorTaskService(
    private val taskRepository: TaskRepository,
    private val fileRepository: FileRepository,
    private val mentorTaskMapper: MentorTaskMapper
) {

    @Transactional(readOnly = true)
    fun getTaskForFeedback(mentorId: Long, taskId: Long): MentorTaskDetailResponse {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)

        return mentorTaskMapper.mapToDetail(task)
    }

    @Transactional
    fun saveTemporary(mentorId: Long, taskId: Long, request: MentorFeedbackRequest) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)
        validateGeneralCommentLength(request.generalComment)

        replaceFeedback(task, request, RegisterStatus.TEMPORARY)
    }

    @Transactional
    fun saveFeedback(mentorId: Long, taskId: Long, request: MentorFeedbackRequest) {
        val task = findTaskWithDetails(taskId)
        validateMentorAccess(mentorId, task)
        validateFinalFeedbackRequest(request)

        replaceFeedback(task, request, RegisterStatus.CONFIRMED)
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
        } else {
            task.generalComment = GeneralComment(content = generalComment)
        }
    }

    // ── 상세 피드백 생성 ────────────────────────────────

    // TODO: 로직 이해하기
    //  Comment와 CommentAnnotation은 양방향 관계이므로 annotation.comment = comment 설정이 필요하다.
    //  Comment는 task.comments와 proofShot.comments 양쪽에 추가해야
    //  Task/ProofShot 어느 쪽에서 조회해도 피드백이 포함된다.
    //  번호(number)는 인증 사진별로 1부터 순차 배정된다.
    private fun createFeedbackComments(task: Task, request: MentorFeedbackRequest, status: RegisterStatus) {
        val proofShotMap = task.proofShots.associateBy { it.id!! }

        for (psRequest in request.proofShotFeedbacks) {
            val proofShot = proofShotMap[psRequest.proofShotId]
                ?: throw IllegalArgumentException(PROOF_SHOT_NOT_FOUND.message)

            for ((index, feedback) in psRequest.feedbacks.withIndex()) {
                val annotation = CommentAnnotation(
                    number = index + 1,
                    percentX = feedback.percentX,
                    percentY = feedback.percentY
                )
                val comment = Comment(
                    task = task,
                    proofShot = proofShot,
                    annotation = annotation,
                    content = feedback.content,
                    starred = feedback.starred,
                    type = CommentType.FEEDBACK,
                    registerStatus = status
                )

                proofShot.comments.add(comment)
                task.comments.add(comment)
            }
        }
    }

    companion object {
        private const val MAX_GENERAL_COMMENT_LENGTH = 200
    }
}
