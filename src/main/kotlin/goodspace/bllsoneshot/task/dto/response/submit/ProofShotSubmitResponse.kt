package goodspace.bllsoneshot.task.dto.response.submit

data class ProofShotSubmitResponse(
    val imageFileId: Long,
    val questions: List<QuestionSubmitResponse>
)
