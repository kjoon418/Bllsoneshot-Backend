package goodspace.bllsoneshot.mentor.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.mentor.dto.request.MentorFeedbackRequest
import goodspace.bllsoneshot.mentor.dto.request.MentorTaskUpdateRequest
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskDetailResponse
import goodspace.bllsoneshot.mentor.service.MentorTaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.security.Principal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mentor Task", description = "멘토 - 할 일 관리")
@RestController
@RequestMapping("/mentors/tasks")
@PreAuthorize("hasRole('MENTOR')")
class MentorTaskController(
    private val mentorTaskService: MentorTaskService
) {

    @GetMapping("/{taskId}")
    @Operation(
        summary = "멘토 할 일 상세 조회",
        description = """
            멘토가 멘티의 할 일을 상세 조회합니다.
            인증 사진, 멘티의 질문, 멘토의 피드백(임시저장 포함)을 모두 반환합니다.
            
            응답 필드:
            generalComment: 멘토의 총평 (null이면 아직 작성하지 않음)
            hasProofShot: 학생 인증 사진 제출 여부
            proofShots.feedbacks: 임시저장(TEMPORARY) + 확정저장(CONFIRMED) 피드백 모두 포함
        """
    )
    fun getTaskDetail(
        @PathVariable taskId: Long,
        principal: Principal
    ): ResponseEntity<MentorTaskDetailResponse> {
        val result = mentorTaskService.getTaskForFeedback(principal.userId, taskId)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{taskId}/feedback/temporary")
    @Operation(
        summary = "멘토 피드백 임시저장",
        description = """
            멘토가 작성 중인 피드백을 임시저장합니다.
            프론트에서 자동 저장(debounce) 시 이 API를 호출해 주시면 됩니다.
            
            임시저장은 검증이 느슨합니다:
            - 총평이 비어 있어도 됩니다.
            - 상세 피드백 내용이 비어 있어도 됩니다.
            - 총평 최대 200자 제한만 적용됩니다.
            
            요청 필드:
            generalComment: 멘토의 총평 (최대 200자, null 가능)
            proofShotFeedbacks: 인증 사진별 피드백 목록
        """
    )
    fun saveTemporary(
        @PathVariable taskId: Long,
        @RequestBody request: MentorFeedbackRequest,
        principal: Principal
    ): ResponseEntity<Void> {
        mentorTaskService.saveTemporary(principal.userId, taskId, request)
        return NO_CONTENT
    }

    @PutMapping("/{taskId}/feedback")
    @Operation(
        summary = "멘토 피드백 최종 저장(피드백 추가)",
        description = """
            멘토가 피드백을 최종 저장합니다.
            멘티에게 피드백이 공개되며, 이후 수정은 이 API를 다시 호출합니다.
            
            최종 저장은 검증이 엄격합니다:
            - 총평은 필수이며 최대 200자입니다.
            - 상세 피드백 내용이 비어 있으면 안 됩니다.
            
            요청 필드:
            generalComment: 멘토의 총평 (필수, 최대 200자)
            proofShotFeedbacks: 인증 사진별 피드백 목록
        """
    )
    fun saveFeedback(
        @PathVariable taskId: Long,
        @RequestBody request: MentorFeedbackRequest,
        principal: Principal
    ): ResponseEntity<Void> {
        mentorTaskService.saveFeedback(principal.userId, taskId, request)
        return NO_CONTENT
    }

    @DeleteMapping("/{taskId}/feedback")
    @Operation(
        summary = "멘토 피드백 삭제",
        description = """
            멘토가 해당 할 일의 피드백을 전부 삭제합니다.
            총평과 상세 피드백(임시저장 + 확정저장)이 모두 삭제됩니다.
            멘티의 질문은 삭제되지 않습니다.
            
            피드백이 없는 할 일에 대해 호출해도 에러 없이 정상 응답합니다.
        """
    )
    fun deleteFeedback(
        @PathVariable taskId: Long,
        principal: Principal
    ): ResponseEntity<Void> {
        mentorTaskService.deleteFeedback(principal.userId, taskId)
        return NO_CONTENT
    }

    @PutMapping("/{taskId}")
    @Operation(
        summary = "멘토 할 일 수정",
        description = """
            멘토가 할 일의 이름과 목표 시간을 수정합니다.
            
            요청 필드:
            taskName: 할 일 이름 (필수)
            goalMinutes: 목표 시간 (분, 0 이상)
        """
    )
    fun updateTask(
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MentorTaskUpdateRequest,
        principal: Principal
    ): ResponseEntity<Void> {
        mentorTaskService.updateTask(principal.userId, taskId, request)
        return NO_CONTENT
    }
}
