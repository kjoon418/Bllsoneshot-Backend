package goodspace.bllsoneshot.file.controller


import goodspace.bllsoneshot.file.dto.FileDownloadResponse
import goodspace.bllsoneshot.file.dto.FileUploadResponse
import goodspace.bllsoneshot.file.service.FileService
import goodspace.bllsoneshot.global.response.NO_CONTENT
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "파일 업로드",
        description = """
            파일을 업로드합니다.
            
            [요청]
            file: 업로드할 파일
            folderPath: 파일이 저장될 폴더 경로 (기본값: /images)
            
            [응답]
            fileId: 업로드된 파일의 고유 ID
            url: 업로드된 파일의 URL
            originalName: 업로드된 파일 원본 이름
            contentType: 업로드된 파일의 종류
        """
    )
    fun uploadFile(
        // ("file") 생략시 스프링이 파라미터 이름 자동 dc추론하지만 명시해 주는 것이 안전하다.
        @RequestPart("file") file: MultipartFile,
        @RequestParam(defaultValue = "/images") folderPath: String
    ): ResponseEntity<FileUploadResponse> {
        val response = fileService.uploadFile(file, folderPath)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    @Operation(
        summary = "파일 삭제",
        description = "파일 ID를 통해 파일을 삭제합니다."
    )
    fun deleteFile(
        @RequestParam fileId: Long
    ): ResponseEntity<Void> {
        fileService.deleteFile(fileId)

        return NO_CONTENT
    }

    @GetMapping
    @Operation(
        summary = "파일 다운로드 URL 생성",
        description = "파일 ID를 통해 파일 다운로드 URL을 생성합니다."
    )
    fun getDownloadUrl(
        @RequestParam fileId: Long
    ): ResponseEntity<FileDownloadResponse> {
        val response = fileService.getDownloadUrl(fileId)

        return ResponseEntity.ok(response)
    }
}
