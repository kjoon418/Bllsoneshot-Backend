package goodspace.bllsoneshot.task.dto.request

import java.time.LocalDate

data class ActualMinutesUpdateRequest(
    val currentDate: LocalDate,
    val actualMinutes: Int?
)
