package jhkim105.tutorials.domain

import jakarta.persistence.*
import jhkim105.tutorials.ColumnLengths
import org.hibernate.annotations.GenericGenerator


@Entity
class User(
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = ColumnLengths.UUID)
    var id: String? = null,

    var username: String,
    var password: String,
    var name: String,
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var company: Company? = null

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
