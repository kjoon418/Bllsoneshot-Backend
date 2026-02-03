package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.Worksheet
import goodspace.bllsoneshot.task.dto.response.feedback.WorksheetResponse
import org.springframework.stereotype.Component

@Component
class WorksheetMapper {

    fun map(worksheet: Worksheet): WorksheetResponse {
        return WorksheetResponse(
            fileId = worksheet.file.id!!,
            fileName = worksheet.file.fileName,
            fileContentType = worksheet.file.contentType
        )
    }
}
