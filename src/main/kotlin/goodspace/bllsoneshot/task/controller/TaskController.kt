package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.MenteeTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.MenteeTaskUpdateRequest
import goodspace.bllsoneshot.task.dto.request.MentorTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.TaskCompleteRequest
import goodspace.bllsoneshot.task.dto.request.TaskSubmitRequest
import goodspace.bllsoneshot.task.dto.response.TaskByDateResponse
import goodspace.bllsoneshot.task.dto.response.TaskDetailResponse
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.dto.response.TasksResponse
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import goodspace.bllsoneshot.task.dto.response.submit.TaskSubmitResponse
import goodspace.bllsoneshot.task.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
            subject: 과목(KOREAN, ENGLISH, MATH, RESOURCE)
        """
    )
    fun getDailyTasks(
        principal: Principal,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
    ): ResponseEntity<TasksResponse> {
        val userId = principal.userId

        val response = taskService.findTasksByDate(userId, date)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/mentee/{menteeId}")
    @Operation(
        summary = "멘티의 할 일 목록 조회(기간 조회)",
        description = """
            해당 기간 내의 할 일을 조회합니다.
            
            담당 멘티에 대해서만 호출할 수 있습니다.
            자료는 조회하지 않습니다.
            
            DTO는 날짜 순서대로 정렬됩니다.

            [요청]
            startDate: 조회 시작 날짜(yyyy-MM-dd)
            endDate: 조회 종료 날짜(yyyy-MM-dd)

            [응답]
            date: 할 일의 날짜(yyyy-MM-dd)
            createdBy: 할 일을 만든 사람(ROLE_MENTOR / ROLE_MENTEE)
            subject: 과목(KOREAN, ENGLISH, MATH)
        """
    )
    fun getTasksOfMentee(
        principal: Principal,
        @PathVariable menteeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<TaskByDateResponse>> {
        val mentorId = principal.userId

        val response = taskService.findTasksOfMentee(mentorId, menteeId, startDate, endDate)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/mentor")
    @Operation(
        summary = "할 일 추가(멘토)",
        description = """
            dates × taskNames 조합만큼 할 일이 생성됩니다.

            [dates]
            "기간으로 받기" 사용 시 프론트에서 시작일~종료일 사이 날짜를 펼쳐서 배열로 보내주세요.
            예) 02-07 ~ 02-09 → dates: ["2026-02-07", "2026-02-08", "2026-02-09"]

            [taskNames]
            할 일 이름 배열 (최소 1개, 각 최대 50자)
        """
    )
    fun addTaskByMentor(
        principal: Principal,
        @Valid @RequestBody request: MentorTaskCreateRequest
    ): ResponseEntity<List<TaskResponse>> {
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

    @PutMapping("/mentee/{taskId}")
    @Operation(
        summary = "할 일 수정(멘티)",
        description = """
            할 일을 수정합니다.
            
            본인의 할 일에 대해서만 호출할 수 있습니다.
            멘티에 의해 만들어진 할 일에 대해서만 호출할 수 있습니다.
            피드백이 달린 할 일에 대해서도 호출할 수 있습니다.
            
            [요청]
            taskName: 할 일의 이름입니다. 비어 있을 수 없습니다.
            goalMinutes: 목표 시간(분)입니다. 0 이상이어야 합니다.
        """
    )
    fun updateTaskByMentee(
        principal: Principal,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MenteeTaskUpdateRequest
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.updateTaskByMentee(userId, taskId, request)

        return NO_CONTENT
    }

    @GetMapping("/{taskId}/details")
    @Operation(
        summary = "할 일 상세조회(바텀시트)",
        description = """
            할 일의 상세 정보를 조회합니다.
            
            본인의 할 일만 조회할 수 있습니다.
        """
    )
    fun getTaskDetails(
        principal: Principal,
        @PathVariable taskId: Long
    ): ResponseEntity<TaskDetailResponse> {
        val userId = principal.userId

        val response = taskService.getTaskDetail(userId, taskId)

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
            subject: 과목(KOREAN, ENGLISH, MATH, RESOURCE)
            proofShots: 인증 사진 목록
            percentX: 주석이 이미지 좌측에서부터 몇 % 떨어진 곳에 있는지 수치
            percentY: 주석이 이미지 상단에서부터 몇 % 떨어진 곳에 있는지 수치
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
            
            [응답]
            percentX: 주석이 이미지 좌측에서부터 몇 % 떨어진 곳에 있는지 수치
            percentY: 주석이 이미지 상단에서부터 몇 % 떨어진 곳에 있는지 수치
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
            본인의 할 일에 대한 피드백만 조회할 수 있으며, 임시 저장 상태(TEMPORARY)인 피드백은 조회되지 않습니다.
            피드백이 있는 할 일에 대해서만 호출할 수 있습니다.

            [null 가능 속성]
            answer: 멘티 답변 미작성 시 null
            
            [응답]
            starred: 해당 피드백이 중요하다고 표시되었는지 여부
            annotation: 이미지 위에 표시되는 주석
            percentX: 주석이 이미지 좌측에서부터 몇 % 떨어진 곳에 있는지 수치
            percentY: 주석이 이미지 상단에서부터 몇 % 떨어진 곳에 있는지 수치
            registerStatus: 피드백 등록 상태(TEMPORARY, REGISTERED)
        """
    )
    fun getTaskFeedback(
        principal: Principal,
        @PathVariable taskId: Long
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
