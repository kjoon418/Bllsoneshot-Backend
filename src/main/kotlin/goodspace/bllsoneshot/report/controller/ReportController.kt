package goodspace.bllsoneshot.report.controller

import goodspace.bllsoneshot.entity.assignment.Subject
import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.report.dto.request.ReportCreateRequest
import goodspace.bllsoneshot.report.dto.response.ReportResponse
import goodspace.bllsoneshot.report.dto.response.ReportTaskResponse
import goodspace.bllsoneshot.report.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.security.Principal
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reports")
@Tag(
    name = "학습리포트 API"
)
class ReportController(
    private val reportService: ReportService
) {

    @PostMapping("mentee/{menteeId}")
    @Operation(
        summary = "학습 리포트 발행",
        description = """
            담당 멘티의 학습 리포트를 새로 생성합니다.
            시작일/종료일/과목이 같은 학습 리포트는 한 멘티에 중복 생성할 수 없습니다.
            총평, 잘한 점, 보완할 점은 최소 1개 이상이며 공백일 수 없습니다.
            
            [요청]
            menteeId: 멘티 ID (Path)
            subject: 과목 (KOREAN, ENGLISH, MATH)
            startDate: 리포트 시작일 (yyyy-MM-dd)
            endDate: 리포트 종료일 (yyyy-MM-dd)
            generalComment: 총평
            goodPoints: 잘한 점 목록
            badPoints: 보완할 점 목록
            
            [응답]
            subject: 과목
            startDate: 리포트 시작일
            endDate: 리포트 종료일
            generalComment: 총평
            goodPoints: 잘한 점 목록
            badPoints: 보완할 점 목록
        """
    )
    fun createLearningReport(
        principal: Principal,
        @PathVariable menteeId: Long,
        @Valid @RequestBody request: ReportCreateRequest
    ): ResponseEntity<ReportResponse> {
        val mentorId = principal.userId

        val response = reportService.createLearningReport(mentorId, menteeId, request)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response)
    }

    @GetMapping("mentee/{menteeId}/subjects/{subject}")
    @Operation(
        summary = "학습 리포트 조회(멘토)",
        description = """
            작성된 학습 리포트의 내용을 조회합니다.
            
            담당 멘티의 학습 리포트만 조회할 수 있습니다.
            
            [요청]
            subject: 과목(KOREAN, ENGLISH, MATH)
            startDate: 리포트 시작일(yyyy-MM-dd)
            endDate: 리포트 종료일(yyyy-MM-dd)
            
            [응답]
            subject: 과목(KOREAN, ENGLISH, MATH)
            startDate: 리포트 시작일
            endDate: 리포트 종료일
            generalComment: 총평
            goodPoints: 잘한 점 목록
            badPoints: 보완할 점 목록
        """
    )
    fun getReport(
        principal: Principal,
        @PathVariable menteeId: Long,
        @PathVariable subject: Subject,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ReportResponse> {
        val mentorId = principal.userId

        val response = reportService.getReport(mentorId, menteeId, subject, startDate, endDate)

        return ResponseEntity.ok(response)
    }

    @GetMapping("mentee/me/subjects/{subject}")
    @Operation(
        summary = "날짜/과목 기반 학습 리포트 조회(멘티)",
        description = """
            멘토가 본인(멘티)에게 작성해준 학습 리포트를 조회합니다.
            과목과 날짜에 해당하는 학습 리포트를 조회합니다.
            
            [요청]
            subject: 과목(KOREAN, ENGLISH, MATH)
            date: 날짜(yyyy-MM-dd)
            
            [응답]
            subject: 과목(KOREAN, ENGLISH, MATH)
            startDate: 리포트 시작일
            endDate: 리포트 종료일
            generalComment: 총평
            goodPoints: 잘한 점 목록
            badPoints: 보완할 점 목록
        """
    )
    fun getReceivedReport(
        principal: Principal,
        @PathVariable subject: Subject,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<ReportTaskResponse> {
        val menteeId = principal.userId

        val response = reportService.getReceivedReport(menteeId, subject, date)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{reportId}")
    @Operation(
        summary = "ID 기반 학습 리포트 조회(멘티)",
        description = """
            멘토가 본인(멘티)에게 작성해준 학습 리포트를 조회합니다.
            식별자에 해당하는 학습 리포트를 조회합니다.
            
            [요청]
            reportId: 학습 리포트 식별자
            
            [응답]
            taskAmount: 전체 과제 수
            completedTaskAmount: 완료한 과제 수
            goalMinutesTotal: 목표 학습 시간(분)
            actualMinutesTotal: 실제 학습 시간(분)
            report: 리포트 정보
                - subject: 과목(KOREAN, ENGLISH, MATH)
                - startDate: 리포트 시작일
                - endDate: 리포트 종료일
                - generalComment: 총평
                - goodPoints: 잘한 점 목록
                - badPoints: 보완할 점 목록
        """
    )
    fun getReceivedReportById(
        principal: Principal,
        @PathVariable reportId: Long,
    ): ResponseEntity<ReportTaskResponse> {
        val menteeId = principal.userId

        val response = reportService.getReceivedReportById(menteeId, reportId)

        return ResponseEntity.ok(response)
    }
}
