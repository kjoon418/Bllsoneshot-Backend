package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.MenteeTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.TaskCompleteUpdateRequest
import jakarta.validation.Valid
import goodspace.bllsoneshot.task.dto.response.TaskResponse
import goodspace.bllsoneshot.task.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.security.Principal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
            
            date: 조회할 날짜(yyyy-MM-dd)
            
            createdBy: 할 일을 만든 사람(ROLE_MENTOR 혹은 ROLE_MENTEE)
            subject: 과목(KOREAN, ENGLISH, MATH)
            generalComment: 피드백(총평) 내용
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

    @PostMapping("/mentee")
    @Operation(
        summary = "할 일 추가(멘티)",
        description = """
            멘티 권한으로 할 일을 추가합니다.
            생성된 할 일에 대한 정보를 응답합니다.
            
            taskName: 할 일 이름
            goalMinutes: 목표 시간(분)
            date: 할 일 날짜(yyyy-MM-dd)
            subject: 과목(KOREAN, ENGLISH, MATH)
        """
    )
    fun addTask(
        principal: Principal,
        @Valid @RequestBody request: MenteeTaskCreateRequest
    ): ResponseEntity<TaskResponse> {
        val userId = principal.userId

        val response = taskService.createTask(userId, request)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{taskId}")
    @Operation(
        summary = "할 일 완료 상태 수정",
        description = """
            할 일의 완료 상태를 변경합니다.
            
            completed: 완료 여부(true/false)
            시간을 기록하지 않은 할 일이라면 완료할 수 없습니다.
        """
    )
    fun updateCompleted(
        principal: Principal,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: TaskCompleteUpdateRequest
    ): ResponseEntity<Void> {
        val userId = principal.userId

        taskService.updateCompleted(userId, taskId, request)

        return NO_CONTENT
    }
}
