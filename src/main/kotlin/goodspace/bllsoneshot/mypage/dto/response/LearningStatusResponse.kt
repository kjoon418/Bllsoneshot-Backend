package goodspace.bllsoneshot.mypage.dto.response

import goodspace.bllsoneshot.entity.assignment.Subject

data class LearningStatusResponse(
    val subject: Subject,
    val taskAmount: Int,
    val completedTaskAmount: Int
)
