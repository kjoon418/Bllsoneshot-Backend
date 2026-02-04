package goodspace.bllsoneshot.task.service

import goodspace.bllsoneshot.entity.assignment.*
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.*
import goodspace.bllsoneshot.repository.file.FileRepository
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.task.dto.request.*
import goodspace.bllsoneshot.task.dto.response.TaskDetailResponse
import goodspace.bllsoneshot.task.dto.response.submit.TaskSubmitResponse
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import goodspace.bllsoneshot.task.mapper.TaskDetailMapper
import goodspace.bllsoneshot.task.mapper.TaskFeedbackMapper
import goodspace.bllsoneshot.task.mapper.TaskSubmitMapper
import goodspace.bllsoneshot.task.mapper.TaskMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val taskMapper: TaskMapper,
    private val taskDetailMapper: TaskDetailMapper,
    private val taskFeedbackMapper: TaskFeedbackMapper,
    private val taskSubmitMapper: TaskSubmitMapper
) {

    @Transactional(readOnly = true)
    fun findTasksByDate(
        userId: Long,
        date: LocalDate
    ): List<TaskResponse> {
        val tasks = taskRepository.findCurrentTasks(userId, date)

        return taskMapper.map(tasks)
    }

    @Transactional
    fun createTaskByMentor(mentorId: Long, request: MentorTaskCreateRequest): List<TaskResponse> {
        val mentee: User = userRepository.findById(request.menteeId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        validateDate(request.dates, request.startDate, request.dueDate)

        val tasks = buildTasks(request, mentee)
        val savedTasks = taskRepository.saveAll(tasks)

        return taskMapper.map(savedTasks)
    }

    @Transactional
    fun createTaskByMentee(userId: Long, request: MenteeTaskCreateRequest): TaskResponse {
        val mentee = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        val task = Task(
            mentee = mentee,
            name = request.taskName,
            startDate = request.date,
            dueDate = request.date,
            goalMinutes = request.goalMinutes,
            subject = request.subject,
            createdBy = UserRole.ROLE_MENTEE
        )

        val savedTask = taskRepository.save(task)

        return taskMapper.map(savedTask)
    }

    @Transactional
    fun getTaskFeedback(userId: Long, taskId: Long): TaskFeedbackResponse {
        val task = taskRepository.findByIdWithMenteeAndGeneralCommentAndProofShots(taskId)
            ?: throw IllegalArgumentException(TASK_NOT_FOUND.message)

        validateTaskOwnership(task, userId)
        validateHasFeedback(task)

        task.markFeedbackAsRead()

        return taskFeedbackMapper.map(task)
    }

    @Transactional(readOnly = true)
    fun getTaskDetail(userId: Long, taskId: Long): TaskDetailResponse {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)

        return taskDetailMapper.map(task)
    }

    @Transactional(readOnly = true)
    fun getTaskForSubmit(userId: Long, taskId: Long): TaskSubmitResponse {
        val task = taskRepository.findByIdWithMenteeAndGeneralCommentAndProofShots(taskId)
            ?: throw IllegalArgumentException(TASK_NOT_FOUND.message)

        validateTaskOwnership(task, userId)
        validateTaskSubmittable(task)

        return taskSubmitMapper.map(task)
    }

    @Transactional
    fun submitTask(userId: Long, taskId: Long, request: TaskSubmitRequest) {
        val task = taskRepository.findByIdWithMenteeAndGeneralCommentAndProofShots(taskId)
            ?: throw IllegalArgumentException(TASK_NOT_FOUND.message)

        validateTaskOwnership(task, userId)
        validateTaskSubmittable(task)

        removeExistingProofShotsAndComments(task)
        createProofShotsAndQuestions(task, request.proofShots)

        taskRepository.save(task)
    }

    @Transactional
    fun updateCompleted(
        userId: Long,
        taskId: Long,
        request: TaskCompleteRequest
    ) {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)
        validateTaskCompletable(task, request.currentDate)

        task.actualMinutes = request.actualMinutes
        task.completed = true
    }

    @Transactional
    fun updateActualMinutes(
        userId: Long,
        taskId: Long,
        request: ActualMinutesUpdateRequest
    ) {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)
        validateTaskCompleted(task)

        task.actualMinutes = request.actualMinutes
    }

    @Transactional
    fun deleteTaskByMentee(userId: Long, taskId: Long) {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)
        validateDeletableByMentee(task)

        taskRepository.delete(task)
    }

    private fun validateTaskCompletable(task: Task, currentDate: LocalDate) {
        val startDate = task.startDate ?: return

        if (startDate.isAfter(currentDate)) {
            throw IllegalStateException(CANNOT_COMPLETE_FUTURE_TASK.message)
        }
    }

    private fun validateDate(
        dates: List<LocalDate>,
        startDate: LocalDate?,
        dueDate: LocalDate?
    ) {
        if (dates.isNotEmpty()) {
            require(startDate == null && dueDate == null) {
                DATE_RANGE_NOT_ALLOWED_WITH_DATES.message
            }
            return
        }

        require(!(startDate == null && dueDate == null)) {
            START_OR_END_DATE_REQUIRED.message
        }
        if (startDate != null && dueDate != null) {
            require(!startDate.isAfter(dueDate)) {
                DATE_INVALID.message
            }
        }
    }

    private fun buildTasks(request: MentorTaskCreateRequest, mentee: User): List<Task> {
        if (request.dates.isEmpty()) {
            val task = Task(
                mentee = mentee,
                subject = request.subject,
                startDate = request.startDate,
                dueDate = request.dueDate,
                name = request.taskName,
                goalMinutes = request.goalMinutes,
                actualMinutes = null,
                createdBy = UserRole.ROLE_MENTOR
            )
            task.worksheets.addAll(
                request.worksheets
                    .mapNotNull { it.fileId }
                    .mapNotNull { fileId ->
                        val file = fileRepository.findById(fileId).orElse(null)
                        file?.let { Worksheet(task, it) }
                    }
            )
            task.columnLinks.addAll(
                request.columnLinks
                    .mapNotNull { it.link?.takeIf { link -> link.isNotBlank() } }
                    .map { link -> ColumnLink(task, link) }
            )
            return listOf(task)
        }

        return request.dates.distinct().map { date ->
            val task = Task(
                mentee = mentee,
                subject = request.subject,
                startDate = date,
                dueDate = date,
                name = request.taskName,
                goalMinutes = request.goalMinutes,
                actualMinutes = null,
                createdBy = UserRole.ROLE_MENTOR
            )
            task.worksheets.addAll(
                request.worksheets
                    .mapNotNull { it.fileId }
                    .mapNotNull { fileId ->
                        val file = fileRepository.findById(fileId).orElse(null)
                        file?.let { Worksheet(task, it) }
                    }
            )
            task.columnLinks.addAll(
                request.columnLinks
                    .mapNotNull { it.link?.takeIf { link -> link.isNotBlank() } }
                    .map { link -> ColumnLink(task, link) }
            )
            task
        }
    }

    private fun removeExistingProofShotsAndComments(task: Task) {
        task.comments.clear()
        task.proofShots.clear()
    }

    private fun createProofShotsAndQuestions(task: Task, proofShotRequests: List<ProofShotRequest>) {
        for (proofShotRequest in proofShotRequests) {
            val file = fileRepository.findById(proofShotRequest.imageFileId)
                .orElseThrow { IllegalArgumentException(FILE_NOT_FOUND.message) }
            val proofShot = ProofShot(task = task, file = file)

            for ((index, question) in proofShotRequest.questions.withIndex()) {
                val annotation = CommentAnnotation(
                    proofShot = proofShot,
                    number = index + 1,
                    percentX = question.percentX.toDouble(),
                    percentY = question.percentY.toDouble()
                )
                val comment = Comment(
                    task = task,
                    proofShot = proofShot,
                    commentAnnotation = annotation,
                    content = question.content,
                    type = CommentType.QUESTION,
                    registerStatus = RegisterStatus.REGISTERED
                )
                annotation.comment = comment

                proofShot.comments.add(comment)
                task.comments.add(comment)
            }

            task.proofShots.add(proofShot)
        }
    }

    private fun findTaskBy(taskId: Long): Task {
        return taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException(TASK_NOT_FOUND.message) }
    }

    private fun validateTaskOwnership(
        task: Task,
        menteeId: Long
    ) {
        check(task.mentee.id == menteeId) { TASK_ACCESS_DENIED.message }
    }

    private fun validateHasFeedback(task: Task) {
        check(task.hasFeedback()) { FEEDBACK_NOT_FOUND.message }
    }

    private fun validateTaskSubmittable(task: Task) {
        check(!task.hasFeedback()) { TASK_NOT_SUBMITTABLE.message }
    }

    private fun validateTaskCompleted(task: Task) {
        check(task.completed) { INCOMPLETED_TASK.message }
    }

    private fun validateDeletableByMentee(task: Task) {
        check(task.createdBy == UserRole.ROLE_MENTEE) { CANNOT_DELETE_MENTOR_CREATED_TASK.message }
    }
}
