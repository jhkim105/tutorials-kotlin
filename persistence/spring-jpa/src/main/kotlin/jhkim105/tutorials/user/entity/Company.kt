package jhkim105.tutorials.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jhkim105.tutorials.ColumnLengths
import org.hibernate.annotations.GenericGenerator

@Entity
class Company(
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = ColumnLengths.UUID)
    var id: String? = null,
    var name: String
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Company) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }


}