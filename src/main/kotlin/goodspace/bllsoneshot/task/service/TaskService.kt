package goodspace.bllsoneshot.task.service

import goodspace.bllsoneshot.entity.assignment.*
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage
import goodspace.bllsoneshot.global.exception.ExceptionMessage.*
import goodspace.bllsoneshot.repository.file.FileRepository
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.task.dto.request.*
import goodspace.bllsoneshot.task.dto.response.TaskByDateResponse
import goodspace.bllsoneshot.task.dto.response.TaskDetailResponse
import goodspace.bllsoneshot.task.dto.response.submit.TaskSubmitResponse
import goodspace.bllsoneshot.task.dto.response.TasksResponse
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import goodspace.bllsoneshot.task.mapper.TaskDetailMapper
import goodspace.bllsoneshot.task.mapper.TaskFeedbackMapper
import goodspace.bllsoneshot.task.mapper.TaskMapper
import goodspace.bllsoneshot.task.mapper.TasksMapper
import goodspace.bllsoneshot.task.mapper.TaskSubmitMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val taskMapper: TaskMapper,
    private val tasksMapper: TasksMapper,
    private val taskDetailMapper: TaskDetailMapper,
    private val taskFeedbackMapper: TaskFeedbackMapper,
    private val taskSubmitMapper: TaskSubmitMapper
) {

    @Transactional(readOnly = true)
    fun findTasksByDate(
        userId: Long,
        date: LocalDate
    ): TasksResponse {
        val tasks = taskRepository.findCurrentTasks(userId, date)

        return tasksMapper.map(tasks)
    }

    @Transactional(readOnly = true)
    fun findTasksOfMentee(
        mentorId: Long,
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TaskByDateResponse> {
        val mentee = userRepository.findById(menteeId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        validateAssignedMentee(mentorId, mentee)

        // TODO: 나중에 task.isResources로 필터링하도록 수정
        val tasks = taskRepository.findDateBetweenTasks(
            menteeId = menteeId,
            startDate = startDate,
            endDate = endDate
        )

        return taskMapper.mapByDate(tasks)
    }

    @Transactional
    fun createTaskByMentor(mentorId: Long, request: MentorTaskCreateRequest): List<TaskResponse> {
        val mentee: User = userRepository.findById(request.menteeId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        validateMentorMenteeRelation(mentee, mentorId)
        validateDate(request.dates)
        validateTaskNames(request.taskNames)

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
            date = request.date,
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
    fun updateTaskByMentee(userId: Long, taskId: Long, request: MenteeTaskUpdateRequest) {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)
        validateUpdatableByMentee(task)

        task.name = request.taskName
        task.goalMinutes = request.goalMinutes
    }

    @Transactional
    fun deleteTaskByMentee(userId: Long, taskId: Long) {
        val task = findTaskBy(taskId)

        validateTaskOwnership(task, userId)
        validateDeletableByMentee(task)

        taskRepository.delete(task)
    }

    private fun validateTaskCompletable(task: Task, currentDate: LocalDate) {
        val date = task.date ?: return

        if (date.isAfter(currentDate)) {
            throw IllegalStateException(CANNOT_COMPLETE_FUTURE_TASK.message)
        }
    }

    private fun validateTaskNames(taskNames: List<String>) {
        require(taskNames.isNotEmpty()) {
            TASK_NAMES_REQUIRED.message
        }
        taskNames.forEach { name ->
            require(name.isNotBlank()) {
                TASK_NAME_BLANK.message
            }
            require(name.length <= MAX_TASK_NAME_LENGTH) {
                TASK_NAME_TOO_LONG.message
            }
        }
    }

    private fun validateDate(
        dates: List<LocalDate>,
    ) {
        require(dates.isNotEmpty()) {
            DATES_REQUIRED.message
        }
        require(dates.size == dates.distinct().size) {
            DUPLICATE_DATES_NOT_ALLOWED.message
        }

        val today = LocalDate.now()
        // dates.none: 요소 하나라도 조건 불만족시 false
        require(dates.none { it.isBefore(today) }) {
            PAST_DATES_NOT_ALLOWED.message
        }
    }

    private fun buildTasks(request: MentorTaskCreateRequest, mentee: User): List<Task> {
        // dates × taskNames 조합으로 각각의 Task 생성
        return request.dates.flatMap { date ->
            request.taskNames.map { taskName ->
                val task = Task(
                    mentee = mentee,
                    subject = request.subject,
                    date = date,
                    name = taskName,
                    goalMinutes = request.goalMinutes,
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
                    percentX = question.percentX,
                    percentY = question.percentY
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
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException(TASK_NOT_FOUND.message) }

        if (task.isResource) {
            throw IllegalArgumentException(TASK_NOT_FOUND.message)
        }

        return task
    }

    private fun validateMentorMenteeRelation(mentee: User, mentorId: Long) {
        check(mentee.mentor?.id == mentorId) { MENTOR_MENTEE_RELATION_DENIED.message }
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

    private fun validateDeletableByMentee(task: Task) {
        check(task.createdBy == UserRole.ROLE_MENTEE) { CANNOT_DELETE_MENTOR_CREATED_TASK.message }
    }

    private fun validateUpdatableByMentee(task: Task) {
        check(task.createdBy == UserRole.ROLE_MENTEE) { CANNOT_UPDATE_MENTOR_CREATED_TASK.message }
    }

    private fun validateAssignedMentee(
        mentorId: Long,
        mentee: User
    ) {
        check(mentee.mentor?.id == mentorId) { MENTEE_ACCESS_DENIED.message }
    }

    companion object {
        private const val MAX_TASK_NAME_LENGTH = 50
    }
}
