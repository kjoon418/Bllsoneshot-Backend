package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Answer(
    @Column(nullable = true)
    var content: String? = null,

    @Column(nullable = true)
    var temporaryContent: String? = null
) : BaseEntity()
