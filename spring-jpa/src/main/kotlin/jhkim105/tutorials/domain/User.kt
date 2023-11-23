package jhkim105.tutorials.domain

import jakarta.persistence.*
import jhkim105.tutorials.ColumnLengths
import org.hibernate.Length
import org.hibernate.annotations.UuidGenerator


@Entity
class User(
    @Id
    @UuidGenerator
    @Column(length = ColumnLengths.UUID)
    var id: String? = null,

    var username: String,
    var password: String,
    var name: String,

    @Column(length = Length.LONG32)
    // column size 에 따라 tinytext, mediumtext, longtext 로 생성됨
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var company: Company? = null,

    // https://hibernate.atlassian.net/browse/HHH-17180
//    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(SqlTypes.VARCHAR)
    // https://discourse.hibernate.org/t/hibernate-6-cannot-persist-enum-as-ordinal-in-varchar-column/7775/10
    @Convert(converter = UserTypeConverter::class)
    @Column(length = 10)
    var userType: UserType? = null


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

enum class UserType {
    ADMIN, USER
}

class UserTypeConverter: EnumConverter<UserType>(UserType::class.java)
