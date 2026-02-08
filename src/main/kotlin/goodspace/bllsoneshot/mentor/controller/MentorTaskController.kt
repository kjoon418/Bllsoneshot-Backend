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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("hasRole('MENTOR')")
@RestController
@RequestMapping("/mentors/tasks")
@Tag(name = "멘토 할 일 관리 API")
class MentorTaskController(
    private val mentorTaskService: MentorTaskService
) {

    @GetMapping("/{taskId}")
    @Operation(
        summary = "멘토 할 일 상세 조회 (피드백 작성/조회 화면)",
        description = """
            멘토가 담당 멘티의 할 일 상세 정보를 조회합니다.
            인증 사진, 멘티의 질문, 작성된 피드백(임시저장 포함)을 모두 포함합니다.
            
            [응답]
            hasFeedback: 최종 등록(REGISTERED)된 피드백 존재 여부
            hasProofShot: 학생 인증 사진 제출 여부
            proofShots.feedbacks: 임시저장(TEMPORARY) + 최종저장(REGISTERED) 피드백 모두 포함
        """
    )
    fun getTaskForFeedback(
        principal: Principal,
        @PathVariable taskId: Long
    ): ResponseEntity<MentorTaskDetailResponse> {
        val mentorId = principal.userId
        val response = mentorTaskService.getTaskForFeedback(mentorId, taskId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{taskId}/feedback")
    @Operation(
        summary = "피드백 저장 (임시저장/최종저장)",
        description = """
            멘토가 멘티의 할 일에 피드백을 저장합니다.
            기존 피드백은 모두 교체됩니다.
            
            registerStatus가 TEMPORARY이면 임시저장(자동 저장),
            REGISTERED이면 최종 피드백 저장입니다.
            
            최종 저장(REGISTERED) 시 generalComment는 필수입니다.
            피드백 번호는 proofShotFeedbacks 내 feedbacks 리스트 순서대로 1부터 배정됩니다.
            
            [요청]
            generalComment: 멘토의 총평 (최대 200자, null 가능)
            registerStatus: TEMPORARY(임시저장) / REGISTERED(최종저장)
            proofShotFeedbacks: 인증 사진별 피드백 목록
              - proofShotId: 인증 사진 ID
              - feedbacks: 상세 피드백 목록
                - content: 피드백 내용
                - starred: 중요 표시 여부
                - percentX: 이미지 좌측 기준 주석 위치 (%)
                - percentY: 이미지 상단 기준 주석 위치 (%)
        """
    )
    fun saveFeedback(
        principal: Principal,
        @PathVariable taskId: Long,
        @RequestBody request: MentorFeedbackRequest
    ): ResponseEntity<Void> {
        val mentorId = principal.userId
        mentorTaskService.saveFeedback(mentorId, taskId, request)
        return NO_CONTENT
    }

    @PutMapping("/{taskId}")
    @Operation(
        summary = "멘토 할 일 수정",
        description = """
            멘토가 담당 멘티의 할 일을 수정합니다.
            
            [요청]
            taskName: 할 일 이름 (비어 있을 수 없음)
            goalMinutes: 목표 시간(분, 0 이상)
        """
    )
    fun updateTask(
        principal: Principal,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MentorTaskUpdateRequest
    ): ResponseEntity<Void> {
        val mentorId = principal.userId
        mentorTaskService.updateTask(mentorId, taskId, request)
        return NO_CONTENT
    }
}
