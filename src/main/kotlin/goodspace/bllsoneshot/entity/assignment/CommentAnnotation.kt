package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
class CommentAnnotation(
    @OneToOne(mappedBy = "commentAnnotation", fetch = FetchType.LAZY)
    var comment: Comment? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val proofShot: ProofShot,

    @Column(nullable = false)
    val number: Int,
    @Column(nullable = false)
    val percentX: Double,
    @Column(nullable = false)
    val percentY: Double
) : BaseEntity()
