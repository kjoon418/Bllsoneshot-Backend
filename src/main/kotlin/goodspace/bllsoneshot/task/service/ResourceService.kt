package goodspace.bllsoneshot.task.service

import goodspace.bllsoneshot.entity.assignment.ColumnLink
import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.entity.assignment.Task
import goodspace.bllsoneshot.entity.assignment.Worksheet
import goodspace.bllsoneshot.entity.user.User
import goodspace.bllsoneshot.entity.user.UserRole
import goodspace.bllsoneshot.global.exception.ExceptionMessage.FILE_NOT_FOUND
import goodspace.bllsoneshot.global.exception.ExceptionMessage.RESOURCE_ACCESS_DENIED
import goodspace.bllsoneshot.global.exception.ExceptionMessage.RESOURCE_SUBJECT_INVALID
import goodspace.bllsoneshot.global.exception.ExceptionMessage.USER_NOT_FOUND
import goodspace.bllsoneshot.repository.file.FileRepository
import goodspace.bllsoneshot.repository.task.TaskRepository
import goodspace.bllsoneshot.repository.user.UserRepository
import goodspace.bllsoneshot.task.dto.request.ResourceCreateRequest
import goodspace.bllsoneshot.task.dto.response.ResourceResponse
import goodspace.bllsoneshot.task.dto.response.ResourcesResponse
import goodspace.bllsoneshot.task.mapper.ResourceMapper
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResourceService(
    private val taskRepository: TaskRepository,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val resourceMapper: ResourceMapper
) {

    @Transactional(readOnly = true)
    fun getResources(mentorId: Long, menteeId: Long): ResourcesResponse {
        val mentee = userRepository.findById(menteeId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        validateMentorAccess(mentorId, mentee)

        val resources = taskRepository.findResourcesByMenteeId(menteeId)

        return ResourcesResponse(resourceMapper.map(resources))
    }

    @Transactional
    fun createResource(mentorId: Long, request: ResourceCreateRequest): ResourceResponse {
        val mentee = userRepository.findById(request.menteeId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND.message) }

        validateMentorAccess(mentorId, mentee)
        validateResourceRequest(request)

        val resource = Task(
            mentee = mentee,
            subject = request.subject,
            date = LocalDate.now(),
            name = request.resourceName,
            goalMinutes = 0,
            createdBy = UserRole.ROLE_MENTOR,
            isResource = true
        )

        request.fileId?.let { fileId ->
            val file = fileRepository.findById(fileId)
                .orElseThrow { IllegalArgumentException(FILE_NOT_FOUND.message) }
            resource.worksheets.add(Worksheet(resource, file))
        }
        request.columnLink
            ?.takeIf { it.isNotBlank() }
            ?.let { link -> resource.columnLinks.add(ColumnLink(resource, link)) }

        val saved = taskRepository.save(resource)

        return resourceMapper.map(saved)
    }

    private fun validateMentorAccess(mentorId: Long, mentee: User) {
        check(mentee.mentor?.id == mentorId) { RESOURCE_ACCESS_DENIED.message }
    }

    private fun validateResourceRequest(request: ResourceCreateRequest) {
        require(request.subject != Subject.RESOURCE) { RESOURCE_SUBJECT_INVALID.message }
    }
}
