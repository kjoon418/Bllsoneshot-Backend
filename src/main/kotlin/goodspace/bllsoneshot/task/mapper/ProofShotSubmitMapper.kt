package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.ProofShot
import goodspace.bllsoneshot.task.dto.response.ProofShotSubmitResponse
import org.springframework.stereotype.Component

@Component
class ProofShotSubmitMapper(
    private val questionSubmitMapper: QuestionSubmitMapper
) {

    fun map(proofShot: ProofShot): ProofShotSubmitResponse {
        val questions = proofShot.questComments
            .sortedBy { it.commentAnnotation.number }
            .map { questionSubmitMapper.map(it) }

        return ProofShotSubmitResponse(
            imageFileId = proofShot.file.id!!,
            questions = questions
        )
    }
}
