package goodspace.bllsoneshot.entity.user

import goodspace.bllsoneshot.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @ManyToOne(fetch = FetchType.LAZY)
    val mentor: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,
    @Column(nullable = false, unique = true)
    val loginId: String,
    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,
    val grade: String? = null,

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    val profileImage: ByteArray? = null
) : BaseEntity() {
    @OneToMany(mappedBy = "mentee", fetch = FetchType.LAZY)
    val subjects: MutableList<MenteeSubject> = mutableListOf()

    @OneToMany(mappedBy = "mentee", fetch = FetchType.LAZY)
    val reports: MutableList<LearningReport> = mutableListOf()

    var refreshToken: String? = null
}
