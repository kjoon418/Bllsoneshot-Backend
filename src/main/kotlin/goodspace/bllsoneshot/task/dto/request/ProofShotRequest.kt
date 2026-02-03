package goodspace.bllsoneshot.task.dto.request

data class ProofShotRequest(
    val imageFileId: Long,
    val questions: List<QuestionRequest>
)
