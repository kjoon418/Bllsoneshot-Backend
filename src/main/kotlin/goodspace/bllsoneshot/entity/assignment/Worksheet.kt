package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
class Worksheet(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val file: File
) : BaseEntity()
