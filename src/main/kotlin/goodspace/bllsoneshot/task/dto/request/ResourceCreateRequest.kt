package goodspace.bllsoneshot.task.dto.request

import goodspace.bllsoneshot.entity.assignment.Subject
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class ResourceCreateRequest(
    val menteeId: Long,
    val subject: Subject,
    @field:NotBlank(message = "자료 이름이 비어 있습니다.")
    val resourceName: String,
    val fileId: Long? = null,
    val columnLink: String? = null,
    val uploadedAt: LocalDate = LocalDate.now()
)
