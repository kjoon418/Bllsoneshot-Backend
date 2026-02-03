package goodspace.bllsoneshot.task.dto.response

data class ProofShotSubmitResponse(
    val imageFileId: Long,
    val questions: List<QuestionSubmitResponse>
)
