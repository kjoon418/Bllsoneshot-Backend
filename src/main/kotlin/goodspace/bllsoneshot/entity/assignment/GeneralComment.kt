package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Entity

@Entity
class GeneralComment(
    var content: String? = null,
    var temporaryContent: String? = null
) : BaseEntity()
