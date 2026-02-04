package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.MenteeTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.MentorTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.ActualMinutesUpdateRequest
import goodspace.bllsoneshot.task.dto.request.TaskCompleteRequest
import goodspace.bllsoneshot.task.dto.request.TaskSubmitRequest
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import jakarta.validation.Valid
import goodspace.bllsoneshot.task.dto.response.TaskSubmitResponse
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.security.Principal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
@Tag(
    name = "할 일 API"
)
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    @Operation(
        summary = "할 일 목록 조회(날짜 기준)",
        description = """
            해당 날짜의 할 일 목록을 조회합니다.

            [요청]
            date: 조회할 날짜(yyyy-MM-dd)

            [응답]
            createdBy: 할 일을 만든 사람(ROLE_MENTOR / ROLE_MENTEE)
            subject: 과목(KOREAN, ENGLISH, MATH)

            [null 가능 속성]
            generalComment: 피드백(총평) 미작성 시 null
            actualMinutes: 실제 소요 시간 미기록 시 null
        """
    )
    fun getDailyTasks(
        principal: Principal,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
    ): ResponseEntity<List<TaskResponse>> {
        val userId = principal.userId

        val response = taskService.findTasksByDate(userId, date)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/mentor")
    @Operation(
        summary = "할 일 추가(멘토)",
        description = """
            멘토 권한으로 할 일을 추가합니다.
            생성된 할 일에 대한 정보를 응답합니다.
        """
    )
    fun addTaskByMentor(
        principal: Principal,
        @Valid @RequestBody request: MentorTaskCreateRequest
    ): ResponseEntity<TaskResponse> {
        val userId = principal.userId

        val response = taskService.createTaskByMentor(userId, request)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/mentee")
    @Operation(
        summary = "할 일 추가(멘티)",
        description = """
            멘티 권한으로 할 일을 추가합니다.
            생성된 할 일에 대한 정보를 응답합니다.
        """
    )
    fun addTaskByMentee(
        principal: Principal,
        @Valid @RequestBody request: MenteeTaskCreateRequest
    ): ResponseEntity<TaskResponse> {
        val userId = principal.userId

        val response = taskService.createTaskByMentee(userId, request)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{taskId}/submit")
    @Operation(
        summary = "할 일 제출 정보 조회",
        description = """
            할 일에 대한 제출 정보를 조회합니다.
            응답의 proofShots 구조를 그대로 수정하여 PUT /tasks/{taskId} 요청 본문으로 사용할 수 있습니다.
            
            이미 멘토가 피드백을 남긴 할 일은 조회할 수 없습니다.

            [응답]
            taskId: 할 일 ID
            name: 할 일 이름
            subject: 과목(KOREAN, ENGLISH, MATH)
            proofShots: 인증 사진 목록
        """
    )
    fun getTaskForSubmit(
        principal: Principal,
        @PathVariable taskId: Long
    ): ResponseEntity<TaskSubmitResponse> {
        val userId = principal.userId

        val response = taskService.getTaskForSubmit(userId, taskId)

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{taskId}/submit")
    @Operation(
        summary = "할 일 제출",
        description = """
            할 일에 인증 사진과 질문을 제출합니다.
            기존의 인증 사진과 질문은 제거됩니다.
            
            이미 멘토가 피드백(총평 제외)을 남긴 할 일에 대해서는 호출할 수 없습니다.
            
            질문의 번호는 리스트의 순서대로 배정됩니다.
            (1부터 시작하는 오름차순)
        """
    )
    fun submitTask(
        principal: Principal,
        @PathVariable taskId: Long,
        @RequestBody request: TaskSubmitRequest
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.submitTask(userId, taskId, request)

        return NO_CONTENT
    }

    @GetMapping("/{taskId}/feedback")
    @Operation(
        summary = "피드백 조회",
        description = """
            ID를 기반으로 할 일의 피드백 정보를 조회합니다.
            본인의 할 일에 대한 피드백만 조회할 수 있으며, 임시 저장 상태인 피드백은 조회되지 않습니다.
            피드백이 있는 할 일에 대해서만 호출할 수 있습니다.

            [null 가능 속성]
            answer: 멘티 답변 미작성 시 null

            [빈 문자열/빈 배열]
            mentorName: 멘토 미배정 시 빈 문자열
            generalComment: 총평 미작성 시 빈 문자열
            proofShots, worksheets, columnLinks: 없으면 빈 배열
        """
    )
    fun getTaskDetails(
        principal: Principal,
        @PathVariable taskId: Long,
    ): ResponseEntity<TaskFeedbackResponse> {
        val userId = principal.userId

        val response = taskService.getTaskFeedback(userId, taskId)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{taskId}/completed")
    @Operation(
        summary = "할 일 완료",
        description = """
            할 일을 완료합니다.
            
            미래의 할 일에 대해선 호출할 수 없습니다.
            본인의 할 일에 대해서만 호출할 수 있습니다.
            
            [요청]
            currentDate: 현재 날짜(yyyy-MM-dd)
            actualMinutes: 학습 시간(분, 0 이상)
        """
    )
    fun updateCompleted(
        principal: Principal,
        @PathVariable taskId: Long,
        @RequestBody request: TaskCompleteRequest
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.updateCompleted(userId, taskId, request)

        return NO_CONTENT
    }

    @PutMapping("/{taskId}/actual-minutes")
    @Operation(
        summary = "학습 시간 수정",
        description = """
            할 일의 학습 시간을 수정합니다.
            
            완료된 할 일에 대해서만 호출할 수 있습니다.
            본인의 할 일에 대해서만 호출할 수 있습니다.
            
            [요청]
            actualMinutes: 학습 시간(분, 0 이상)
        """
    )
    fun updateActualMinutes(
        principal: Principal,
        @PathVariable taskId: Long,
        @RequestBody request: ActualMinutesUpdateRequest
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.updateActualMinutes(userId, taskId, request)

        return NO_CONTENT
    }

    @DeleteMapping("/{taskId}/mentee")
    @Operation(
        summary = "할 일 삭제(멘티)",
        description = """
            할 일을 삭제합니다.
            
            멘티가 만든 할 일만 삭제할 수 있습니다.
            본인의 할 일만 삭제할 수 있습니다.
        """
    )
    fun deleteTaskByMentee(
        principal: Principal,
        @PathVariable taskId: Long
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.deleteTaskByMentee(userId, taskId)

        return NO_CONTENT
    }
}
