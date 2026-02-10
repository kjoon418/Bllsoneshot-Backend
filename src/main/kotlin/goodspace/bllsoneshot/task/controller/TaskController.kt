package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.global.response.NO_CONTENT
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.MenteeTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.MenteeTaskUpdateRequest
import goodspace.bllsoneshot.task.dto.request.MentorTaskCreateRequest
import goodspace.bllsoneshot.task.dto.request.TaskCompleteRequest
import goodspace.bllsoneshot.task.dto.request.TaskSubmitRequest
import goodspace.bllsoneshot.task.dto.response.task.TaskByDateResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskDetailResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskResponse
import goodspace.bllsoneshot.task.dto.response.task.TasksResponse
import goodspace.bllsoneshot.task.dto.response.feedback.TaskFeedbackResponse
import goodspace.bllsoneshot.mentor.dto.response.MentorTaskFormResponse
import goodspace.bllsoneshot.task.dto.response.submit.TaskSubmitResponse
import goodspace.bllsoneshot.task.dto.response.task.TaskAmountResponse
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
            includeResources: 자료도 포함해 조회할지 여부(기본값 false)

            [응답]
            createdBy: 할 일을 만든 사람(ROLE_MENTOR / ROLE_MENTEE)
            subject: 과목(KOREAN, ENGLISH, MATH, RESOURCE)
        """
    )
    fun getDailyTasks(
        principal: Principal,
        @RequestParam(defaultValue = "false")
        includeResources: Boolean,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
    ): ResponseEntity<TasksResponse> {
        val userId = principal.userId

        val response = taskService.findTasksByDate(userId, includeResources, date)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/duration")
    @Operation(
        summary = "할 일 목록 조회(기간 조회)",
        description = """
            해당 기간 내의 할 일을 조회합니다.(전달한 과목에 대해서만 조회합니다)
            
            본인 혹은 담당 멘티에 대해서만 호출할 수 있습니다.(menteeId를 전달하지 않는다면, 본인의 할 일을 조회합니다)
            
            DTO는 날짜 순서대로 정렬됩니다.

            [요청]
            startDate: 조회 시작 날짜(yyyy-MM-dd)
            endDate: 조회 종료 날짜(yyyy-MM-dd)
            subject: 과목(KOREAN, ENGLISH, MATH)

            [응답]
            date: 할 일의 날짜(yyyy-MM-dd)
            createdBy: 할 일을 만든 사람(ROLE_MENTOR / ROLE_MENTEE)
            subject: 과목(KOREAN, ENGLISH, MATH)
        """
    )
    fun getTasksByDuration(
        principal: Principal,
        @RequestParam(required = false) menteeId: Long?,
        @RequestParam subject: Subject,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<TaskByDateResponse>> {
        val userId = principal.userId

        val response = taskService.findTasksByDuration(userId, menteeId, subject, startDate, endDate)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/duration/amount")
    @Operation(
        summary = "할 일 개수 조회(월간 캘린더)",
        description = """
            해당 기간 내의 완료되지 않은 날짜별 할 일 개수를 조회합니다.
            자료(RESOURCE)의 개수는 포함하지 않습니다.
            
            본인에 대해서만 호출할 수 있습니다.
            
            DTO는 날짜 순서대로 정렬됩니다.

            [요청]
            startDate: 조회 시작 날짜(yyyy-MM-dd)
            endDate: 조회 종료 날짜(yyyy-MM-dd)

            [응답]
            date: 할 일의 날짜(yyyy-MM-dd)
            taskAmount: 할 일의 개수
        """
    )
    fun getTaskAmounts(
        principal: Principal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<TaskAmountResponse>> {
        val userId = principal.userId

        val response = taskService.findTaskAmounts(userId, startDate, endDate)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{taskId}/edit")
    @Operation(
        summary = "멘토 할 일 수정 폼 조회",
        description = """
            멘토가 담당 멘티의 할 일 수정 화면에 필요한 정보를 조회합니다.
            과목, 날짜, 할 일 이름, 목표 시간, 학습 자료(PDF·칼럼 링크)를 반환합니다.
            
            생성 시 dates × taskNames 조합으로 여러 Task가 만들어지므로,
            수정 화면에서도 동일한 폼 구조(리스트)를 사용합니다.
            단일 Task 조회 시에는 각각 원소가 1개인 리스트로 반환됩니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            
            [응답]
            subject: 과목 (KOREAN, ENGLISH, MATH)
            dates: 날짜 목록
            taskNames: 할 일 이름 목록
            goalMinutes: 목표 시간 (분)
            worksheets: 학습 자료 PDF 파일 목록
            columnLinks: 학습 자료 칼럼 링크 목록
        """
    )
    fun getTaskForEdit(
        principal: Principal,
        @PathVariable taskId: Long
    ): ResponseEntity<MentorTaskFormResponse> {
        val userId = principal.userId

        val response = taskService.getTaskForEdit(userId, taskId)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/mentor")
    @Operation(
        summary = "할 일 추가(멘토)",
        description = """
            멘토가 담당 멘티에게 할 일을 추가합니다.
            dates × taskNames 조합만큼 할 일이 생성됩니다.
            
            [요청]
            menteeId: 멘티 ID
            subject: 과목 (KOREAN, ENGLISH, MATH)
            dates: 날짜 배열 (시작일~종료일 사이를 펼쳐서 전달)
            taskNames: 할 일 이름 배열 (최소 1개, 각 최대 50자)
            goalMinutes: 목표 시간 (분, 0 이상)
            worksheets: 학습 자료 목록 (선택)
            columnLinks: 칼럼 링크 목록 (선택)
            
            [응답]
            생성된 할 일 목록
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
            멘티가 본인의 할 일을 추가합니다.

            [요청]
            subject: 과목 (KOREAN, ENGLISH, MATH)
            date: 날짜 (yyyy-MM-dd)
            taskName: 할 일 이름 (최대 50자)
            goalMinutes: 목표 시간 (분, 0 이상)
            
            [응답]
            생성된 할 일 정보
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
            멘티가 본인이 등록한 할 일을 수정합니다.
            멘토가 등록한 할 일은 수정할 수 없습니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            taskName: 할 일 이름 (필수, 최대 50자)
            goalMinutes: 목표 시간 (분, 0 이상)
            
            [응답]
            204 NO CONTENT
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
            
            [요청]
            taskId: 할 일 ID (Path)
            
            [응답]
            taskId: 할 일 ID
            taskName: 할 일 이름
            subject: 과목
            goalMinutes: 목표 시간 (분)
            completed: 완료 여부
            worksheets: 학습 자료 목록
            columnLinks: 칼럼 링크 목록
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
            응답의 proofShots 구조를 그대로 수정하여 PUT /tasks/{taskId}/submit 요청 본문으로 사용할 수 있습니다.
            이미 멘토가 피드백을 남긴 할 일은 조회할 수 없습니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            
            [응답]
            taskId: 할 일 ID
            name: 할 일 이름
            subject: 과목 (KOREAN, ENGLISH, MATH)
            proofShots: 인증 사진 목록
                percentX: 주석 X 위치 (이미지 좌측 기준 %)
                percentY: 주석 Y 위치 (이미지 상단 기준 %)
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
            기존의 인증 사진과 질문은 제거되고 새로 교체됩니다.
            이미 멘토가 피드백을 남긴 할 일에 대해서는 호출할 수 없습니다.
            질문 번호는 리스트 순서대로 1부터 배정됩니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            proofShots: 인증 사진 목록
                imageFileId: 이미지 파일 ID
                questions: 질문 목록
                    content: 질문 내용
                    percentX: 주석 X 위치 (이미지 좌측 기준 %)
                    percentY: 주석 Y 위치 (이미지 상단 기준 %)
            
            [응답]
            204 NO CONTENT
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
            할 일의 피드백 정보를 조회합니다.
            본인의 할 일에 대한 피드백만 조회할 수 있습니다.
            임시 저장 상태(TEMPORARY)인 피드백은 조회되지 않습니다.
            피드백이 있는 할 일에 대해서만 호출할 수 있습니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            
            [응답]
            starred: 중요 표시 여부
            annotation: 이미지 주석 정보
                percentX: 주석 X 위치 (이미지 좌측 기준 %)
                percentY: 주석 Y 위치 (이미지 상단 기준 %)
            registerStatus: 피드백 저장 상태 (CONFIRMED)
            answer: 멘티 답변 (미작성 시 null)
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
            할 일을 완료 처리합니다.
            본인의 할 일에 대해서만 호출할 수 있습니다.
            미래의 할 일은 완료할 수 없습니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            currentDate: 현재 날짜 (yyyy-MM-dd)
            actualMinutes: 실제 학습 시간 (분, 0 이상)
            
            [응답]
            204 NO CONTENT
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
            멘티가 본인이 등록한 할 일을 삭제합니다.
            멘토가 등록한 할 일은 삭제할 수 없습니다.
            
            [요청]
            taskId: 할 일 ID (Path)
            
            [응답]
            204 NO CONTENT
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
