package goodspace.bllsoneshot.mentor.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse
import java.time.LocalDate

/**
 * 멘토가 할 일 수정 화면에서 기존 할 일 정보를 조회할 때 사용하는 응답 DTO.
 *
 * 생성 시 dates × taskNames 조합으로 여러 Task가 만들어지므로,
 * 수정 화면에서도 동일한 폼 구조(리스트)를 사용한다.
 * 단일 Task 조회 시에는 각각 원소가 1개인 리스트로 반환된다.
 */
data class MentorTaskFormResponse(
    val subject: Subject,
    val dates: List<LocalDate>,
    val taskNames: List<String>,
    val goalMinutes: Int,
    val worksheets: List<WorksheetResponse>,
    val columnLinks: List<ColumnLinkResponse>,
)
