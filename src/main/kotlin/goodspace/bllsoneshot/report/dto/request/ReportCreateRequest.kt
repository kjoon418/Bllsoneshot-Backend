package goodspace.bllsoneshot.report.dto.request

import goodspace.bllsoneshot.entity.assignment.Subject
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ReportCreateRequest(
    val subject: Subject,

    val startDate: LocalDate,
    val endDate: LocalDate,

    @field:NotBlank(message = "총평을 입력해 주세요.")
    val generalComment: String,
    @field:Size(min = 1, message = "잘한 점은 최소 1개 이상 입력해 주세요.")
    val goodPoints: List<String>,
    @field:Size(min = 1, message = "보완할 점은 최소 1개 이상 입력해 주세요.")
    val badPoints: List<String>
)
