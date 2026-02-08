package goodspace.bllsoneshot.mentor.controller

import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.mentor.dto.response.MenteeInfoResponse
import goodspace.bllsoneshot.mentor.service.MentorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.security.Principal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("hasRole('MENTOR')")
@RestController
@RequestMapping("/mentor")
@Tag(name = "멘토 API")
class MentorController(
    private val mentorService: MentorService
) {
    @GetMapping("/mentee/{menteeId}")
    @Operation(
        summary = "멘티의 간단한 정보 조회",
        description = """
            멘토가 담당하는 특정 멘티의 기본 정보를 조회합니다.
            
            [요청]
            menteeId: 조회할 멘티의 ID (path variable)
            
            [응답]
            menteeId: 멘티 ID
            menteeName: 멘티 이름
            grade: 멘티 학년
            subjects: 관리 과목 목록 (복수 가능) — KOREAN, ENGLISH, MATH
        """
    )
    fun getMenteeInfo(
        principal: Principal,
        @PathVariable menteeId: Long
    ): ResponseEntity<MenteeInfoResponse> {
        val mentorId = principal.userId
        val response = mentorService.getMenteeInfo(mentorId, menteeId)
        return ResponseEntity.ok(response)
    }
}
