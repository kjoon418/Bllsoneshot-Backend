package goodspace.bllsoneshot.file.service

import goodspace.bllsoneshot.entity.assignment.File
import goodspace.bllsoneshot.file.dto.FileDownloadResponse
import goodspace.bllsoneshot.file.dto.FileUploadResponse
import goodspace.bllsoneshot.repository.file.FileRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URLEncoder
import java.time.Duration
import java.util.*

@Service
class FileService(
    private val s3Client: S3Client,
    private val fileRepository: FileRepository,
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${app.file.max-bytes}") private val maxBytes: Long
) {
    companion object {
        private val ALLOWED_TYPES = setOf("image/png", "image/jpeg", "application/pdf")
        private val logger = KotlinLogging.logger {}
    }

    @Transactional
    fun uploadFile(file: MultipartFile, folder: String): FileUploadResponse {
        val contentType = resolveContentType(file)
        validateFile(file, contentType)

        val uploaded = uploadToS3(file, folder, contentType)

        try {
            val savedFile = fileRepository.save(
                File(
                    fileName = uploaded.fileName,
                    contentType = uploaded.contentType,
                    byteSize = uploaded.byteSize,
                    bucketName = uploaded.bucketName,
                    objectKey = uploaded.objectKey
                )
            )
            val url = makePresignedUrl(uploaded.objectKey, uploaded.fileName)
            return FileUploadResponse(savedFile.id!!, url, savedFile.fileName, savedFile.contentType)
        } catch (e: Exception) {
            deleteFromS3(uploaded.objectKey)
            throw e
        }
    }

    @Transactional
    fun deleteFile(fileId: Long) {
        val file = fileRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException("파일을 찾을 수 없습니다") }

        // TODO: 삭제 순서로 인해 s3에 고아 객체가 남을 수 있는 문제 고민
        fileRepository.delete(file)
        deleteFromS3(file.objectKey)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(fileId: Long): FileDownloadResponse {
        val file = fileRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException("파일을 찾을 수 없습니다") }

        val presignedUrl = makePresignedUrl(file.objectKey, file.fileName)

        return FileDownloadResponse(
            fileId = fileId,
            url = presignedUrl,
            fileName = file.fileName
        )
    }

    /**
     * 클라이언트가 전송한 Content-Type을 우선 사용하되,
     * null이거나 application/octet-stream(바이너리 기본 타입)인 경우
     * 파일 확장자 기반으로 MIME 타입을 추론한다.
     *
     * 근거: 일부 HTTP 클라이언트(프론트엔드 라이브러리 등)가 PDF 파일의
     * Content-Type을 누락하거나 application/octet-stream으로 전송하는 경우가 있다.
     * 이미지 파일은 브라우저가 정확한 Content-Type을 설정하지만,
     * PDF 등 비이미지 파일은 클라이언트 구현에 따라 누락될 수 있다.
     */
    private fun resolveContentType(file: MultipartFile): String? {
        val clientType = file.contentType
            ?.substringBefore(';')  // 파라미터 제거 (예: "application/pdf; charset=utf-8" → "application/pdf")
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() && it != MediaType.APPLICATION_OCTET_STREAM_VALUE }

        if (clientType != null) return clientType

        // 클라이언트 Content-Type이 없거나 generic인 경우, 파일 확장자로 추론
        return MediaTypeFactory.getMediaType(file.originalFilename ?: "")
            .map { it.toString() }
            .orElse(null)
    }

    private fun validateFile(file: MultipartFile, contentType: String?) {
        require(!file.isEmpty) { "파일이 비어 있습니다." }
        require(contentType in ALLOWED_TYPES) {
            "지원하지 않는 파일 타입입니다. (type=$contentType, 허용: $ALLOWED_TYPES)"
        }
        require(file.size <= maxBytes) { "파일 용량이 너무 큽니다." }
    }

    private fun uploadToS3(file: MultipartFile, folder: String, contentType: String?): UploadedFile {
        val objectKey = buildObjectKey(file, folder)
        val safeContentType = contentType ?: "application/octet-stream"

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(safeContentType)
            .contentLength(file.size)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))

        return UploadedFile(
            objectKey = objectKey,
            fileName = file.originalFilename ?: "file",
            contentType = safeContentType,
            byteSize = file.size,
            bucketName = bucket
        )
    }

    private fun buildObjectKey(file: MultipartFile, folder: String): String {
        // 메서드 체이닝 이용한 확장자 추출, ?. 사용시 null이면 바로 null 반환, null 아니면 메서드 체이닝 지속
        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }

        val name = UUID.randomUUID().toString().replace("-", "").substring(0, 7)
        val safeFolder = folder.trim('/')

        return if (extension == null) "$safeFolder/$name" else "$safeFolder/$name.$extension"
    }

    private fun deleteFromS3(objectKey: String) {
        try {
            val request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build()
            s3Client.deleteObject(request)
        } catch (e: Exception) {
            logger.warn("S3 delete failed. key=$objectKey", e)
        }
    }

    private fun makePresignedUrl(objectKey: String, fileName: String): String {
        val disposition = buildContentDisposition(fileName)

        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .responseContentDisposition(disposition)
            .build()

        val presigned = s3Presigner.presignGetObject {
            it.getObjectRequest(request)
            it.signatureDuration(Duration.ofMinutes(10))
        }

        return presigned.url().toString()
    }

    /**
     * RFC 6266 / RFC 5987에 맞는 Content-Disposition 헤더 값을 생성한다.
     *
     * - filename : ASCII 전용 폴백으로, 비ASCII 문자를 '_'로 대체하여 구형 브라우저를 지원한다.
     * - filename* : UTF-8 퍼센트 인코딩된 원본 파일명으로, 한글 등 비ASCII 파일명을 정확히 표현한다.
     */
    private fun buildContentDisposition(fileName: String): String {
        val utf8Encoded = URLEncoder.encode(fileName, Charsets.UTF_8)
            .replace("+", "%20")

        val asciiFallback = fileName
            .replace(Regex("[^\\x20-\\x7E]"), "_")
            .replace("\"", "_")

        return "attachment; filename=\"$asciiFallback\"; filename*=UTF-8''$utf8Encoded"
    }
}

// 코틀린은 특정 역할의 보조 역할을 하는 클래스를 정의하는 것을 권장한다.
data class UploadedFile(
    val objectKey: String,
    val fileName: String,
    val contentType: String,
    val byteSize: Long,
    val bucketName: String
)
