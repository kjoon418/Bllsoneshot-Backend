package goodspace.bllsoneshot.global.file

import goodspace.bllsoneshot.entity.assignment.File
import goodspace.bllsoneshot.global.file.dto.FileDownloadResponse
import goodspace.bllsoneshot.global.file.dto.FileUploadResponse
import goodspace.bllsoneshot.repository.file.FileRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration
import java.util.*

@Service
class FileStorageService(
    private val s3Client: S3Client,
    private val fileRepository: FileRepository,
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${aws.s3.cdn-url}") private val cdnUrl: String
) {
    companion object {
        private val ALLOWED_TYPES = setOf("image/png", "image/jpeg", "application/pdf")
        private const val MAX_BYTES = 10L * 1024 * 1024 // 10MB
        private val logger = KotlinLogging.logger {}
    }

    @Transactional
    fun uploadFile(file: MultipartFile, folder: String): FileUploadResponse {
        validateFile(file)
        require(folder.isNotBlank()) { "폴더 이름은 비어 있을 수 없습니다" }

        val uploaded = uploadToS3(file, folder)

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
            val url = "${cdnUrl.trimEnd('/')}/${uploaded.objectKey}"
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

        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(file.objectKey)
            .build()

        // 다운로드 가능한 임시 URL생성 (10분만 유효)
        val presigned = s3Presigner.presignGetObject {
            it.getObjectRequest(request)
            it.signatureDuration(Duration.ofMinutes(10))
        }

        return FileDownloadResponse(
            fileId = fileId,
            url = presigned.url().toString()
        )
    }

    private fun validateFile(file: MultipartFile) {
        require(!file.isEmpty) { "파일이 비어 있습니다." }
        require(file.contentType in ALLOWED_TYPES) { "지원하지 않는 파일 타입입니다." }
        require(file.size <= MAX_BYTES) { "파일 용량이 너무 큽니다." }
    }

    private fun uploadToS3(file: MultipartFile, folder: String): UploadedFile {
        val objectKey = buildObjectKey(file, folder)
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(file.contentType ?: "application/octet-stream") // null 일 땐 바이너리 기본타입
            .contentLength(file.size)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))

        return UploadedFile(
            objectKey = objectKey,
            fileName = file.originalFilename ?: "file",
            contentType = file.contentType ?: "application/octet-stream",
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

        val name = UUID.randomUUID().toString()
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
}

// 코틀린은 특정 역할의 보조 역할을 하는 클래스를 정의하는 것을 권장한다.
data class UploadedFile(
    val objectKey: String,
    val fileName: String,
    val contentType: String,
    val byteSize: Long,
    val bucketName: String
)
