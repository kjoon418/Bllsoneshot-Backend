package goodspace.bllsoneshot.task.controller

import goodspace.bllsoneshot.global.security.userId
import goodspace.bllsoneshot.task.dto.request.ResourceCreateRequest
import goodspace.bllsoneshot.task.dto.response.ResourceResponse
import goodspace.bllsoneshot.task.dto.response.ResourcesResponse
import goodspace.bllsoneshot.task.service.ResourceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.security.Principal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
            subject: 과목(KOREAN, ENGLISH, MATH)
            registeredDate: 등록일
        """
    )
    fun getResources(
        principal: Principal,
        @RequestParam menteeId: Long
    ): ResponseEntity<ResourcesResponse> {
        val mentorId = principal.userId
        val response = resourceService.getResources(mentorId, menteeId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(
        summary = "자료 등록",
        description = """
            특정 멘티의 자료를 등록합니다.

            [요청]
            menteeId: 멘티 ID
            subject: 과목(KOREAN, ENGLISH, MATH)
            resourceName: 자료 이름
            fileId: PDF 파일 ID(선택)
            columnLink: 칼럼 링크(선택)
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
}
