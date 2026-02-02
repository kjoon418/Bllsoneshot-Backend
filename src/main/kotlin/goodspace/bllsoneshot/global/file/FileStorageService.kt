package goodspace.bllsoneshot.global.file

import goodspace.bllsoneshot.entity.assignment.File
import goodspace.bllsoneshot.global.file.dto.FileUploadResponse
import goodspace.bllsoneshot.repository.file.FileRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class FileStorageService(
    private val s3Client: S3Client,
    private val fileRepository: FileRepository,
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${aws.s3.cdn-url}") private val cdnUrl: String
) {
    @Transactional
    fun uploadFile(file: MultipartFile): FileUploadResponse {
        val uploaded = uploadToS3(file)

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

    private fun uploadToS3(file: MultipartFile): UploadedFile {
        val objectKey = "worksheets/${UUID.randomUUID()}_${file.originalFilename}"
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(file.contentType ?: "application/octet-stream")
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

    private fun deleteFromS3(objectKey: String) {
        val request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build()

        s3Client.deleteObject(request)
    }
}

data class UploadedFile(
    val objectKey: String,
    val fileName: String,
    val contentType: String,
    val byteSize: Long,
    val bucketName: String
)