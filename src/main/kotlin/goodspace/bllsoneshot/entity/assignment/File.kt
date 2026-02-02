package goodspace.bllsoneshot.entity.assignment

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "files")
class File(
    @Column(nullable = false)
    val fileName: String,
    @Column(nullable = false)
    val contentType: String,
    @Column(nullable = false)
    val byteSize: Long,
    @Column(nullable = false)
    val bucketName: String,
    @Column(nullable = false)
    val objectKey: String
) : BaseEntity()
