package goodspace.bllsoneshot.file.dto

data class FileUploadResponse(
    val fileId: Long,
    val url: String,
    val originalName: String,
    val contentType: String
)