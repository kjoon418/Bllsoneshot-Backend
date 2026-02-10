package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.ResourceCreateRequest
import goodspace.bllsoneshot.task.dto.request.ResourceUpdateRequest
import goodspace.bllsoneshot.task.dto.response.submit.ResourceResponse
import goodspace.bllsoneshot.task.dto.response.resource.ResourceSummaryResponse
import goodspace.bllsoneshot.task.service.ResourceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.security.Principal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("hasRole('MENTOR')")
@RestController
@RequestMapping("/resources")
@Tag(name = "자료 관리 API")
class ResourceController(
    private val resourceService: ResourceService
) {

    @GetMapping
    @Operation(
        summary = "자료 목록 조회",
        description = """
            특정 멘티의 자료 목록을 조회합니다.

            [요청]
            menteeId: 멘티 ID

            [응답]
            resourceId: 자료 ID
            resourceName: 자료 이름
            registeredDate: 등록일
            subject: 과목(KOREAN, ENGLISH, MATH)
        """
    )
    fun getResources(
        principal: Principal,
        @RequestParam menteeId: Long
    ): ResponseEntity<List<ResourceSummaryResponse>> {
        val mentorId = principal.userId
        val response = resourceService.getResources(mentorId, menteeId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{resourceId}")
    @Operation(
        summary = "자료 상세 조회",
        description = """
            특정 멘티의 자료 하나를 상세 조회합니다.
            
            [요청]
            resourceId: 자료 ID

            [응답]
            resourceId: 자료 ID
            subject: 과목(KOREAN, ENGLISH, MATH)
            resourceName: 자료 이름
            registeredDate: 등록일
            worksheets: 학습 자료(워크시트) 목록
            columnLinks: 학습 자료(칼럼 링크) 목록
        """
    )
    fun getResourceDetail(
        principal: Principal,
        @RequestParam resourceId: Long
    ): ResponseEntity<ResourceResponse> {
        val mentorId = principal.userId
        val response = resourceService.getResourceDetail(mentorId, resourceId)

        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(
        summary = "자료 등록",
        description = """
            특정 멘티의 자료를 등록합니다.

            [요청]
            menteeId: 멘티 ID
            subject: 과목 (KOREAN, ENGLISH, MATH)
            resourceName: 자료 이름
            fileId: PDF 파일 ID (선택)
            columnLink: 칼럼 링크 (선택)
            uploadedAt: 업로드 날자 (안넣으면 오늘)
            
            [응답]
            생성된 자료 정보
        """
    )
    fun createResource(
        principal: Principal,
        @Valid @RequestBody request: ResourceCreateRequest
    ): ResponseEntity<ResourceResponse> {
        val mentorId = principal.userId
        val response = resourceService.createResource(mentorId, request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{resourceId}")
    @Operation(
        summary = "자료 수정",
        description = """
            특정 자료를 수정합니다.
            기존 학습 자료는 제거되고 새로운 자료로 교체됩니다.

            [요청]
            resourceId: 자료 ID (Path)
            subject: 과목 (KOREAN, ENGLISH, MATH)
            resourceName: 자료 이름
            fileId: PDF 파일 ID (선택)
            columnLink: 칼럼 링크 (선택)
            
            [응답]
            수정된 자료 정보
        """
    )
    fun updateResource(
        principal: Principal,
        @PathVariable resourceId: Long,
        @Valid @RequestBody request: ResourceUpdateRequest
    ): ResponseEntity<ResourceResponse> {
        val mentorId = principal.userId
        val response = resourceService.updateResource(mentorId, resourceId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{resourceId}")
    @Operation(
        summary = "자료 삭제",
        description = """
            특정 자료를 삭제합니다.
            해당 자료에 연결된 학습 자료(파일 참조, 링크)도 함께 삭제됩니다.
            
            [요청]
            resourceId: 자료 ID (Path)
            
            [응답]
            204 NO CONTENT
        """
    )
    fun deleteResource(
        principal: Principal,
        @PathVariable resourceId: Long
    ): ResponseEntity<Void> {
        val mentorId = principal.userId
        resourceService.deleteResource(mentorId, resourceId)
        return ResponseEntity.noContent().build()
    }
}
