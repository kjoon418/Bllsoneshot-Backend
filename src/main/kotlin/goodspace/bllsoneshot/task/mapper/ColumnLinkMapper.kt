package goodspace.bllsoneshot.task.mapper

import goodspace.bllsoneshot.entity.assignment.ColumnLink
import goodspace.bllsoneshot.task.dto.response.feedback.ColumnLinkResponse
import org.springframework.stereotype.Component

@Component
class ColumnLinkMapper {

    fun map(columnLink: ColumnLink): ColumnLinkResponse {
        return ColumnLinkResponse(
            link = columnLink.link
        )
    }
}
