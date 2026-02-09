package goodspace.bllsoneshot.mentor.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.mentor.dto.request.MentorFeedbackRequest
import goodspace.bllsoneshot.mentor.dto.request.MentorTaskUpdateRequest
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskDetailResponse
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskEditResponse
import goodspace.bllsoneshot.mentor.service.MentorTaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

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
            인증 사진, 멘티의 질문, 멘토의 최종 저장된 피드백을 반환합니다.
            임시저장된 피드백을 조회하려면 GET /{taskId}/feedback/temporary API를 사용하세요.
            
            응답 필드:
            generalComment: 멘토의 최종 저장된 총평 (아직 작성하지 않았을 경우 null)
            proofShots.feedbacks: 최종 저장된(CONFIRMED) 피드백만 포함 (임시저장 제외)
        """
    )
    fun getTaskDetail(
        @PathVariable taskId: Long,
        principal: Principal
    ): ResponseEntity<MentorTaskDetailResponse> {
        val result = mentorTaskService.getTaskForFeedback(principal.userId, taskId)

        return ResponseEntity.ok(result)
    }

    @GetMapping("/{taskId}/feedback/temporary")
    @Operation(
        summary = "멘토 피드백 임시저장 조회",
        description = """
            멘토가 임시저장한 피드백을 조회합니다.
            TEMPORARY 상태의 총평(temporaryContent) 및 피드백만 반환됩니다.
            최종 저장된(CONFIRMED) 피드백은 포함되지 않습니다.
            
            응답 필드:
            generalComment: 임시저장된 총평 (temporaryContent)
            proofShots.feedbacks: TEMPORARY 상태의 피드백만 포함
        """
    )
    fun getTemporaryFeedback(
        @PathVariable taskId: Long,
        principal: Principal
    ): ResponseEntity<MentorTaskDetailResponse> {
        val result = mentorTaskService.getTemporaryFeedback(principal.userId, taskId)

        return ResponseEntity.ok(result)
    }

    @PutMapping("/{taskId}/feedback/temporary")
    @Operation(
        summary = "멘토 피드백 임시저장",
        description = """
            멘토가 작성 중인 피드백을 임시저장합니다.
            프론트에서 자동 저장(debounce) 시 이 API를 호출해 주시면 됩니다.
            임시저장은 기존 최종 저장된(CONFIRMED) 피드백에 영향을 주지 않습니다.
            
            임시저장은 검증이 느슨합니다:
            - 총평이 비어 있어도 됩니다.
            - 상세 피드백 내용이 비어 있어도 됩니다.
            - 질문 답변이 비어 있어도 됩니다.
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
            
            최종 저장 시:
            - 기존 최종 저장된(CONFIRMED) 피드백과 임시저장된(TEMPORARY) 피드백이 모두 제거됩니다.
            - 새로운 피드백이 CONFIRMED 상태로 저장됩니다.
            - 모든 임시저장된 답변(temporaryContent)이 제거되고, 새로운 답변이 content에 저장됩니다.
            
            최종 저장은 검증이 엄격합니다:
            - 총평은 필수이며 최대 200자입니다.
            - 상세 피드백 내용이 비어 있으면 안 됩니다.
            - 질문 답변 내용이 비어 있으면 안 됩니다.
            
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
            멘토가 담당 멘티의 할 일을 수정합니다.
            멘토가 등록한 할 일뿐 아니라 멘티가 등록한 할 일도 수정할 수 있습니다.
            
            [요청]
            subject: 과목 (KOREAN, ENGLISH, MATH)
            taskName: 할 일 이름 (필수, 최대 50자)
            goalMinutes: 목표 시간 (분, 0 이상)
            worksheets: 학습 자료 목록 (기존 자료를 전체 교체합니다)
            columnLinks: 칼럼 링크 목록 (기존 링크를 전체 교체합니다)
            
            [응답]
            subject: 과목
            date: 날짜
            taskName: 할 일 이름
            goalMinutes: 목표 시간 (분)
            worksheets: 학습 자료 PDF 파일
            columnLinks: 학습 자료 칼럼 링크
            completed: 멘티가 완료했는지 여부
        """
    )
    fun updateTask(
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MentorTaskUpdateRequest,
        principal: Principal
    ): ResponseEntity<MentorTaskEditResponse> {
        val response = mentorTaskService.updateTask(principal.userId, taskId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{taskId}")
    @Operation(
        summary = "멘토 할 일 삭제",
        description = """
            멘토가 본인이 등록한 할 일을 삭제합니다.
            멘토가 등록한 할 일(createdBy = ROLE_MENTOR)만 삭제할 수 있습니다.
            멘티가 등록한 할 일은 삭제할 수 없습니다.
            
            [요청]
            taskId
            
            [응답]
            204 NO CONTENT
        """
    )
    fun deleteTask(
        @PathVariable taskId: Long,
        principal: Principal
    ): ResponseEntity<Void> {
        mentorTaskService.deleteTask(principal.userId, taskId)

        return NO_CONTENT
    }
}
